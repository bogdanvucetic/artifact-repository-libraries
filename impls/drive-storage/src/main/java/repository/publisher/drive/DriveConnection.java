package repository.publisher.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import repository.common.RepositoryException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

// prvi put otvara browser za prijavu, posle koristi sacuvan token iz tokens/
public class DriveConnection {

    private static final String APP_NAME = "sk-repo";
    private static final JsonFactory JSON = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE_FILE);

    public static Drive connect() {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize(transport);
            return new Drive.Builder(transport, JSON, credential)
                    .setApplicationName(APP_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new RepositoryException("Ne moze da se poveze na Google Drive: " + e.getMessage(), e);
        }
    }

    private static Credential authorize(NetHttpTransport transport) throws IOException {
        Path credentialsFile = Path.of(env("GOOGLE_DRIVE_CREDENTIALS", "config/credentials.json"));
        if (!Files.exists(credentialsFile)) {
            throw new RepositoryException("Nema fajla sa kredencijalima: " + credentialsFile.toAbsolutePath());
        }

        GoogleClientSecrets secrets;
        try (Reader reader = Files.newBufferedReader(credentialsFile)) {
            secrets = GoogleClientSecrets.load(JSON, reader);
        }

        File tokensDir = new File(env("GOOGLE_DRIVE_TOKENS", "tokens"));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokensDir))
                .setAccessType("offline")
                .build();

        // lokalni server na 127.0.0.1 i slobodnom portu, na njega se browser vraca posle prijave
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("127.0.0.1")
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}