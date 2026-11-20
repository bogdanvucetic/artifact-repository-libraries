package resolver.raf;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import repository.common.RepositoryException;
import repository.common.RepositoryReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// todo:
//  - dodao logging zavisnost da ne bi iskakao onaj warning za sshj?
public class RafRepositoryReader implements RepositoryReader, AutoCloseable {
    private SSHClient ssh;
    private SFTPClient sftp;
    private String root;

    public RafRepositoryReader(String ip, String username, String password, String location) {
        try {
            ssh = new SSHClient();
            ssh.loadKnownHosts();
            ssh.connect(ip);
            ssh.authPassword(username, password);
            sftp = ssh.newSFTPClient();
            root = "rn772025-rn782025/" + location;
        } catch (Exception e) {
            e.printStackTrace();
            // throw new RepositoryException("Neuspesno povezivanje na RAF server");
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
