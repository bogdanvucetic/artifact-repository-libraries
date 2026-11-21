package cli.publisher;

import repository.common.*;
import repository.publisher.ComponentBuilder;
import repository.publisher.Publisher;
import repository.publisher.RepositoryConfigBuilder;

import java.nio.file.Path;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static Publisher publisher;
    private static RepositoryConfigBuilder configBuilder = new RepositoryConfigBuilder();
    private static ComponentBuilder componentBuilder;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Upotreba: publisher <implementacija>");
            return;
        }

        ServiceLoader<StorageProvider> loader = ServiceLoader.load(StorageProvider.class);

        StorageProvider provider = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.name().equals(args[0]))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            System.out.println("Nepoznata implementacija: " + args[0]);
            return;
        }

        Storage storage = provider.create(Map.of());
        publisher = new Publisher(storage);

        try {
            System.out.println("Uspesno povezan na " + provider.name());
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

            switch (choice) {
                case "config" -> config(input);
                case "create" -> create(input);
                case "open" -> open(input);
                case "id" -> id(input);
                case "dependency" -> dependency(input);
                case "publish" -> publish(input);
                case "list" -> list(input);
                case "help" -> help();
                case "exit" -> running = false;
                default -> System.out.println("Nevalidna opcija");
            }
        }

        System.out.println("Dovidjenja :)");
        scanner.close();
    }

    private static void config(String[] input) {
        if (input.length < 2) {
            System.out.println("Upotreba: config extensions <extensions|scopes|clear>");
            return;
        }

        try {
            switch (input[1]) {
                case "extensions" -> {
                    if (input.length < 3) {
                        System.out.println("Upotreba: config extensions <extensions>...");
                        return;
                    }

                    configBuilder.addExtensions(
                            Arrays.asList(input).subList(2, input.length)
                    );
                    System.out.println("Uspesno dodate ekstenzije");
                }
                case "scopes" -> {
                    if (input.length < 4 || input.length % 2 != 0) {
                        System.out.println("Upotreba: config scopes <scope> <bool>...");
                        return;
                    }

                    Map<String, Boolean> scopes = new HashMap<>();

                    for (int i = 2; i < input.length; i += 2) {
                        String value = input[i + 1];

                        if (!value.equalsIgnoreCase("true")
                                && !value.equalsIgnoreCase("false"))
                            throw new IllegalArgumentException("Ocekivano true ili false: " + value);

                        scopes.put(input[i], Boolean.parseBoolean(value));
                    }

                    configBuilder.addScopes(scopes);
                    System.out.println("Uspesno dodati scope-ovi");
                }
                case "clear" -> {
                    if (input.length != 2) {
                        System.out.println("Upotreba: config clear");
                        return;
                    }

                    configBuilder = new RepositoryConfigBuilder();
                    System.out.println("Konfiguracija obrisana");
                }
                default -> System.out.println("Nepoznata config opcija: " + input[1]);
            }
        } catch (Exception e) {
            System.out.println("Greska: " + e.getMessage());
        }
    }

    private static void create(String[] input) {
        if (input.length != 2) {
            System.out.println("Upotreba: create <path>");
            return;
        }

        try {
            RepositoryConfig config = configBuilder.build();
            publisher.create(input[1], config);
            componentBuilder = null;
            System.out.println("Uspesno kreiran repozitorjum");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void open(String[] input) {
        if (input.length != 2) {
            System.out.println("Upotreba: open <path>");
            return;
        }

        try {
            publisher.open(input[1]);
            componentBuilder = null;
            System.out.println("Uspesno otvoren repozitorijum");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void id(String[] input) {
        if (input.length != 4) {
            System.out.println("Upotreba: id <organization> <name> <version>");
            return;
        }

        try {
            ComponentBuilder builder = new ComponentBuilder();
            builder.id(input[1], input[2], input[3]);

            componentBuilder = builder;
            System.out.println("Uspesno postavljen ID komponente");
        } catch (Exception e) {
            System.out.println("Greska: " + e.getMessage());
        }
    }

    private static void dependency(String[] input) {
        if (input.length != 6) {
            System.out.println("Upotreba: dependency <organization> <name> <version> <scope> <optional>");
            return;
        }

        if (componentBuilder == null) {
            System.out.println("Prvo definisite ID komponente");
            return;
        }

        try {
            RepositoryConfig repoConfig = publisher.getConfig();

            if (repoConfig == null) {
                System.out.println("Prvo otvorite ili kreirajte repozitorijum");
                return;
            }

            if (!repoConfig.scopes().containsKey(input[4]))
                throw new IllegalArgumentException("Nepodrzan scope: " + input[4]);

            if (!input[5].equalsIgnoreCase("true")
                    && !input[5].equalsIgnoreCase("false"))
                throw new IllegalArgumentException("Optional mora biti true ili false");

            ComponentId id = new ComponentId(input[1], input[2], input[3]);

            Dependency dependency = new Dependency(
                    id,
                    input[4],
                    Boolean.parseBoolean(input[5])
            );

            componentBuilder.addDependency(dependency);

            System.out.println("Uspesno dodata zavisnost: " + dependency);
        } catch (Exception e) {
            System.out.println("Greska: " + e.getMessage());
        }
    }

    private static void publish(String[] input) {
        if (input.length != 2) {
            System.out.println("Upotreba: publish <file>");
            return;
        }

        if (componentBuilder == null) {
            System.out.println("Prvo definisite ID komponente");
            return;
        }

        try {
            publisher.publish(componentBuilder.build(), Path.of(input[1]));
            componentBuilder = null;
            System.out.println("Uspesno objavljena komponenta");
        } catch (Exception e) {
            System.out.println("Greska: " + e.getMessage());
        }
    }

    private static void list(String[] input) {
        try {
            List<String> results;

            switch (input.length) {
                case 1 -> results = publisher.listAllOrganizations();
                case 2 -> results = publisher.listAllProjectsInOrganization(
                        input[1]);
                case 3 -> results = publisher.listAllProjectVersions(
                        input[1], input[2]);
                case 4 -> results = publisher.listAllProjectDependencies(
                        input[1], input[2], input[3]).stream().map(Objects::toString).toList();
                default -> {
                    System.out.println("Upotreba: list [organization] [name] [version]");
                    return;
                }
            }

            if (results.isEmpty()) {
                System.out.println("Nema rezultata");
                return;
            }

            for (String result : results)
                System.out.println(result);

        } catch (Exception e) {
            System.out.println("Greska: " + e.getMessage());
        }
    }

    private static void help() {
        System.out.println("""
                Dostupne komande:
                  config extensions <extension1> <extension2> ...
                  config scopes <scope1> <bool1> <scope2> <bool2> ...
                  config clear
                  create <path>
                  open <path>
                  id <organization> <name> <version>
                  dependency <organization> <name> <version> <scope> <optional>
                  publish <file>
                  list [organization] [name] [version]
                  help
                  exit
                """);
    }
}
