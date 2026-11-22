package publisher.raf;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import repository.common.RepositoryException;
import repository.common.Storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RafStorage implements Storage, AutoCloseable {
    private SSHClient ssh;
    private SFTPClient sftp;
    private static final String BASE_ROOT = "rn772025-rn782025";

    private String root;

    public RafStorage(String ip, String username, String password) {
        SSHClient client = new SSHClient();

        try {
            client.loadKnownHosts();
            client.connect(ip);
            client.authPassword(username, password);

            SFTPClient sftpClient = client.newSFTPClient();

            ssh = client;
            sftp = sftpClient;
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException closeError) {
                throw new RepositoryException("Neuspesno zatvaranje konekcije na RAF server", e);
            }

            throw new RepositoryException("Neuspesno povezivanje na RAF server", e);
        }
    }

    @Override
    public void createRepositoryAt(String location) {
        String path = repositoryPath(location);

        try {
            if (sftp.statExistence(path) != null)
                throw new RepositoryException("Repozitorijum vec postoji: " + path);

            sftp.mkdir(path);
            root = path;
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno kreiranje repozitorijuma: " + path, e);
        }
    }

    @Override
    public void openRepositoryAt(String location) {
        String path = repositoryPath(location);

        try {
            FileAttributes attributes = sftp.statExistence(path);

            if (attributes == null || attributes.getType() != FileMode.Type.DIRECTORY)
                throw new RepositoryException("Repozitorijum ne postoji: " + path);

            root = path;
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno otvaranje repozitorijuma: " + path, e);
        }
    }

    @Override
    public void createDirectories(String path) {
        try {
            sftp.mkdirs(resolve(path));
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno kreiranje direktorijuma: " + path, e);
        }
    }

    @Override
    public void createFile(String relativePath, InputStream content) {
        try (RemoteFile file = sftp.open(
                resolve(relativePath),
                EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.EXCL)
        );
             OutputStream out = file.new RemoteFileOutputStream()) {

            content.transferTo(out);

        } catch (IOException e) {
            throw new RepositoryException("Neuspesno upisivanje fajla: " + relativePath, e);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        try {
            return sftp.statExistence(resolve(relativePath)) != null;
        } catch (IOException e) {
            throw new RepositoryException("Neuspesna provera postojanja: " + relativePath, e);
        }
    }

    @Override
    public List<String> listDirectories(String relativePath) {
        return list(relativePath, true);
    }

    @Override
    public List<String> listFiles(String relativePath) {
        return list(relativePath, false);
    }

    @Override
    public InputStream read(String relativePath) {
        try (RemoteFile file = sftp.open(resolve(relativePath))) {
            InputStream input = file.new RemoteFileInputStream();
            byte[] content = input.readAllBytes();

            return new ByteArrayInputStream(content);
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno citanje: " + relativePath, e);
        }
    }

    @Override
    public void close() {
        try {
            sftp.close();
            ssh.close();
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno zatvaranje konekcije", e);
        }
    }

    private List<String> list(String relativePath, boolean directories) {
        String path = resolve(relativePath);
        List<String> result = new ArrayList<>();

        try {
            FileAttributes attributes = sftp.statExistence(path);

            if (attributes == null || attributes.getType() != FileMode.Type.DIRECTORY)
                return result;

            for (RemoteResourceInfo item : sftp.ls(path)) {
                String name = item.getName();

                if (name.equals(".") || name.equals(".."))
                    continue;

                if (directories && item.isDirectory())
                    result.add(name);
                else if (!directories && item.isRegularFile())
                    result.add(name);
            }

            return result;
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno listanje: " + relativePath, e);
        }
    }

    private static String repositoryPath(String location) {
        if (location == null || location.isBlank()
                || location.contains("/")
                || location.contains("\\")
                || location.equals(".")
                || location.equals("..")) {
            throw new RepositoryException("Nevalidno ime repozitorijuma: " + location);
        }

        return BASE_ROOT + "/" + location;
    }

    private String resolve(String relativePath) {
        if (root == null)
            throw new RepositoryException("Repozitorijum nije otvoren");

        if (relativePath == null)
            throw new RepositoryException("Putanja ne sme biti null");

        if (relativePath.isEmpty())
            return root;

        if (relativePath.startsWith("/") || relativePath.contains("\\"))
            throw new RepositoryException("Nevalidna putanja: " + relativePath);

        String[] parts = relativePath.split("/");

        for (String part : parts) {
            if (part.isEmpty() || part.equals(".") || part.equals(".."))
                throw new RepositoryException("Nevalidna putanja: " + relativePath);
        }

        return root + "/" + relativePath;
    }
}
