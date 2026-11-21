package cli.resolver;

import dependency.resolver.DefaultDependencyResolver;
import repository.common.ComponentId;
import repository.common.Storage;
import repository.common.StorageProvider;
import resolver.api.ConflictStrategy;
import resolver.api.DependencyResolver;
import resolver.api.ResolutionResult;
import resolver.api.ResolveOptions;

import java.nio.file.Path;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static DependencyResolver resolver;
    private static Storage storage;
    private static StorageProvider provider;
    private static boolean repositoryOpened = false;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Upotreba: resolver <implementacija>");
            return;
        }

        ServiceLoader<StorageProvider> loader = ServiceLoader.load(StorageProvider.class);

        provider = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.name().equals(args[0]))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            System.out.println("Nepoznata implementacija: " + args[0]);
            return;
        }

        storage = provider.create(Map.of());
        resolver = new DefaultDependencyResolver(storage);

        try {
            System.out.println("Izabrana implementacija: " + provider.name());
            menu();
        } finally {
            if (storage instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    System.out.println("Greska pri zatvaranju: " + e.getMessage());
                }
            }
        }
    }

    private static void menu() {
        boolean running = true;
        boolean first = true;

        while (running) {
            if (first) {
                help();
                first = false;
            }

            if (!scanner.hasNextLine())
                break;

            String line = scanner.nextLine().trim();

            if (line.isEmpty())
                continue;

            String[] input = line.split("\\s+");
            String choice = input[0];

            try {
                switch (choice) {
                    case "open" -> open(input);
                    case "download" -> download(input);
                    case "download-file" -> downloadFromFile(input);
                    case "help" -> help();
                    case "exit" -> running = false;
                    default -> System.out.println("Nevalidna opcija");
                }
            } catch (Exception e) {
                System.out.println("Greska: " + e.getMessage());
            }
        }

        System.out.println("Dovidjenja :)");
        scanner.close();
    }

    private static void open(String[] input) {
        if (input.length != 2) {
            System.out.println("Upotreba: open <path>");
            return;
        }

        resolver.open(input[1]);
        repositoryOpened = true;
        System.out.println("Uspesno otvoren repozitorijum: " + input[1]);
    }

    private static void download(String[] input) {
        if (input.length < 5 || input.length > 7) {
            System.out.println("Upotreba: download <organization> <name> <version> <targetDir> [newest|nearest] [true|false]");
            return;
        }

        if (!repositoryOpened) {
            System.out.println("Prvo otvorite repozitorijum");
            return;
        }

        ComponentId id = new ComponentId(input[1], input[2], input[3]);
        ResolveOptions options = parseOptions(input, 4);

        ResolutionResult result = resolver.resolve(List.of(id), options);

        System.out.println("Uspesno preuzeto komponenti: " + result.downloaded().size());
    }

    private static void downloadFromFile(String[] input) {
        if (input.length < 3 || input.length > 5) {
            System.out.println("Upotreba: download-file <file> <targetDir> [newest|nearest] [true|false]");
            return;
        }

        if (!repositoryOpened) {
            System.out.println("Prvo otvorite repozitorijum");
            return;
        }

        ResolveOptions options = parseOptions(input, 2);

        ResolutionResult result = resolver.resolveFromFile(Path.of(input[1]), options);

        System.out.println("Uspesno preuzeto komponenti: " + result.downloaded().size());
    }

    private static ResolveOptions parseOptions(String[] input, int targetIndex) {
        Path targetDir = Path.of(input[targetIndex]);

        ConflictStrategy strategy = ConflictStrategy.NEWEST_VERSION;
        boolean includeOptionalRoots = true;

        if (input.length > targetIndex + 1) {
            strategy = switch (input[targetIndex + 1].toLowerCase()) {
                case "newest" -> ConflictStrategy.NEWEST_VERSION;
                case "nearest" -> ConflictStrategy.NEAREST_TO_ROOT;
                default -> throw new IllegalArgumentException(
                        "Nepoznata strategija: " + input[targetIndex + 1]
                );
            };
        }

        if (input.length > targetIndex + 2) {
            String value = input[targetIndex + 2];

            if (!value.equalsIgnoreCase("true") &&
                    !value.equalsIgnoreCase("false"))
                throw new IllegalArgumentException("includeOptionalRoots mora biti true ili false");

            includeOptionalRoots = Boolean.parseBoolean(value);
        }

        return new ResolveOptions(targetDir, strategy, includeOptionalRoots);
    }

    private static void help() {
        System.out.println("""
                Dostupne komande:
                  open <path>
                  download <organization> <name> <version> <targetDir> [strategy] [includeOptionalRoots]
                  download-file <file> <targetDir> [strategy] [includeOptionalRoots]
                  help
                  exit
                """);
    }
}
