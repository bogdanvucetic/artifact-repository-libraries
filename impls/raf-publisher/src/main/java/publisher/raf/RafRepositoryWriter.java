package publisher.raf;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import repository.common.RepositoryException;
import repository.common.RepositoryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;

// todo:
//  - da ima provere i sl. da ne moze da izadje iz rn772025-rn782025 foldera
//  - metode sve ciju implementaciju nisam uradio jos
//  - dodao logging zavisnost da ne bi iskakao onaj warning za sshj?
public class RafRepositoryWriter implements RepositoryWriter, AutoCloseable {
    private SSHClient ssh;
    private SFTPClient sftp;
    private String root;

    public RafRepositoryWriter(String ip, String username, String password) {
        try {
            ssh = new SSHClient();
            ssh.loadKnownHosts();
            ssh.connect(ip);
            ssh.authPassword(username, password);
            sftp = ssh.newSFTPClient();
            root = "rn772025-rn782025";
        } catch (Exception e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno povezivanje na RAF server");
        }
    }

    @Override
    public void createRepositoryAt(String location) {
        try {
            sftp.mkdir(root + "/" + location);
            root = root + "/" + location;
            System.out.println("Uspesno kreiranje repozitorijuma na " + root + "/" + location);
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno kreiranje repozitorijuma na " + root + "/" + location);
        }
    }

    @Override
    public void openRepositoryAt(String location) {
        try {
            sftp.stat(root + "/" + location);
            root = root + "/" + location;
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno otvaranje repozitorijuma", e);
        }
    }

    @Override
    public void createDirectories(String path) {
        try {
            sftp.mkdirs(root + "/" + path);
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno kreiranje direktorijuma", e);
        }
    }

    @Override
    public void writeNew(String relativePath, InputStream content) {
        try (RemoteFile file = sftp.open(
                root + "/" + relativePath,
                EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.EXCL)
        );
             OutputStream out = file.new RemoteFileOutputStream()) {

            content.transferTo(out);

        } catch (IOException e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno upisivanje fajla", e);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        try {
            return sftp.statExistence(root + "/" + relativePath) != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            // throw new RepositoryException("Neuspesna provera da li fajl postoji", e);
        }
    }

    @Override
    public List<String> listDirectories(String relativePath) {
        return List.of();
    }

    @Override
    public List<String> listFiles(String relativePath) {
        return List.of();
    }

    @Override
    public InputStream read(String relativePath) {
        try {
            return sftp.open(root + "/" + relativePath).new RemoteFileInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
            // throw new RepositoryException("Neuspesno citanje iz nekog razloga");
        }
    }

    @Override
    public void close() {
        try {
            ssh.close();
            sftp.close();
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno zatvaranje SSH ili SFTP", e);
        }
    }
}
