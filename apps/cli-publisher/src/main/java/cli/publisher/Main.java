package cli.publisher;

import repository.common.*;
import repository.publisher.Publisher;
import repository.publisher.PublisherProvider;

import java.nio.file.Path;
import java.util.*;

/*
TODO:
 - da ne baci unsupported scope tek pri publish nego odmah pri dodavanju u cli
 - da se komponenta gradi kao component id <organization> <name> <version>,
   component add <organization> <name> <version> <scope> <optional>
   i tako lakse epreko builder-a, i onda u publish() da se zove build
 - sve razne potrebne provere radi robustnosti u metodama
 - create: robust argument handling
   - [ ] if (input.length < 2)
   - [ ] if (input[1] == null
 - publish: robust argument handling
   - [ ] if (input.length < 2)
   - [ ] if (input[1] == null
 - open: robust argument handling
   - [ ] if (input.length < 2)
   - [ ] if (input[1] == null
 - metadata: robust argument handling
 - AKO IMAM VREMENA: bolje poruke tipa
   - create: Uspesno kreiran repozitorijum na <abs-path>
 - AKO IMAM VREMENA: umesto menija da bude niz komandi i da se otvoreni repozitorijum
   cuva u nekom fajlu na sistemu, i otuda povlaci
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static Publisher publisher;
    private static RepositoryConfig config;
    private static Component component;

    public static void main(String[] args) {
        ServiceLoader<PublisherProvider> loader = ServiceLoader.load(PublisherProvider.class);

        PublisherProvider provider = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.name().equals(args[0]))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Nepoznat tip repozitorijuma"));

        publisher = new Publisher(provider.newWriter(null));

        menu();
    }

    private static void menu() {
        boolean running = true;
        boolean first = true;

        while (running) {
            if (first) {
                firstEntry();
                first = false;
            }

            String[] input = scanner.nextLine().trim().split(" ");
            String choice = input[0];

            switch (choice) {
                case "config" -> config(input);
                case "create" -> create(input);
                case "open" -> open(input);
                case "metadata" -> metadata(input);
                case "publish" -> publish(input);
                case "help" -> help();
                case "exit" -> running = false;
                default -> System.out.println("Nevalidna opcija");
            }
        }

        System.out.println("Dovidjenja :)");
        scanner.close();
    }

    private static void config(String[] input) {
        Set<String> allowedExtensions = new HashSet<>();
        Map<String, Boolean> scopes = new HashMap<>();

        int i = 1;

        while (!input[i].equals("--scopes")) {
            allowedExtensions.add(input[i]);
            i += 1;
        }

        i += 1;

        while (i < input.length) {
            String[] entry = input[i].split(":");
            String scope = entry[0];
            Boolean isTransitive = Boolean.parseBoolean(entry[1]);
            scopes.put(scope, isTransitive);
            i += 1;
        }

        config = new RepositoryConfig(allowedExtensions, scopes);
        System.out.println(config);
    }

    private static void create(String[] input) {
        try {
            publisher.create(input[1], config);
            System.out.println("Uspesno kreiran repozitorjum");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void open(String[] input) {
        try {
            publisher.open(input[1]);
            System.out.println("Uspesno otvoren repozitorijum");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void metadata(String[] input) {
        ComponentId id;
        List<Dependency> dependencies = new ArrayList<>();

        id = new ComponentId(input[1], input[2], input[3]);

        for (int i = 4; i < input.length; i++) {
            String[] elem = input[i].split(":");
            ComponentId dependencyId = new ComponentId(elem[0], elem[1], elem[2]);
            Dependency dependency = new Dependency(dependencyId, elem[3], Boolean.parseBoolean(elem[4]));
            dependencies.add(dependency);
        }

        component = new Component(id, dependencies);
        System.out.println(component);

        System.out.println("Uspesno napravljena komponenta");
    }

    private static void publish(String[] input) {
        try {
            publisher.publish(component, Path.of(input[1]));
            System.out.println("Uspesno objavljena komponenta");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void firstEntry() {
        System.out.println("""
                ================================================
                      OBJAVLJIVANJE SOFTVERSKIH KOMPONENTI
                ================================================
                """);
        help();
    }

    private static void help() {
        System.out.println("""
                Dostupne komande:
                  config <extensions> --scopes <name:pulledAsTransitive>
                  create <path>
                  open <path>
                  metadata <organization> <name> <version> <dependencies: organization:name:version:scope:optional>
                  publish <file>
                  help
                  exit
                
                Postupak za koriscenje:
                1. Kreirajte novi repozitorijum ili se povezite na postojeci,
                    Ukoliko kreirate novi repozitorijum neophodno je prvo da definisete konfiguracioni fajl
                2. Definiste metapodatke za komponentu (naziv organizacije, naziv projekta i verzija),
                3. Objavite komponentu,
                4. Ponovite postupak 2-3 ili zavrsite sa objavljivanjem.
                """);
    }
}
