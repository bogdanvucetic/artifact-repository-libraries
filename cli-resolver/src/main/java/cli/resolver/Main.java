package cli.resolver;

import dependency.resolver.DefaultDependencyResolver;
import repository.common.ComponentId;
import resolver.api.ConflictStrategy;
import resolver.api.DependencyResolver;
import resolver.api.ResolveOptions;
import resolver.api.ResolverProvider;

import java.nio.file.Path;
import java.util.*;

// todo: BAGOVI:
//  - open za RAF server otvori repozitorijum koji ne postoji
//
// todo:
//  - da proverava da li je otvoren fajl repozitorijum (da li ima config.json)
//    i da li uspeva da ga parsira i sve sl. pre nego sto kaze uspesno
//  - gdrive i rafov server nece imati podrsku za otvaranje repozitorijuma,
//    tkd vrv. bi bilo najbolje nekako to da izmenim ovde tako da ne zove prvo
//    open()
//  - handling za komponente sa imenima koje imaju razmake
//  - AKO IMAM VREMENA: da svaki lokalni/gdrive/raf repo ima svoje help klase
//    ciji je zadatak da daju custom help i path za komande i sl.
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static DependencyResolver resolver;
    private static ResolverProvider provider;

    public static void main(String[] args) {
        ServiceLoader<ResolverProvider> loader = ServiceLoader.load(ResolverProvider.class);

        provider = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.name().equals(args[0]))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Nepoznat tip repozitorijuma"));

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
                case "open" -> open(input);
                case "download" -> download(input);
                case "help" -> help();
                case "exit" -> running = false;
                default -> System.out.println("Nevalidna opcija");
            }
        }

        System.out.println("Dovidjenja :)");
        scanner.close();
    }

    private static void open(String[] input) {
        resolver = new DefaultDependencyResolver(provider.open(input[1], null));
        System.out.println("Uspesno otvoren folder na putanji " + input[1]);
    }

    private static void download(String[] input) {
        /// download organization name id targetDir conflictStrategy includeOptionalRoots

        ComponentId id = new ComponentId(input[1], input[2], input[3]);
        ConflictStrategy strategy = input[5].equalsIgnoreCase("nearest") ?
                ConflictStrategy.NEAREST_TO_ROOT : ConflictStrategy.NEWEST_VERSION;

        ResolveOptions options = new ResolveOptions(Path.of(input[4]), strategy, Boolean.parseBoolean(input[6]));
        resolver.resolve(List.of(id), options);
        System.out.println("Uspeh!! :D");
    }

    private static void firstEntry() {
        System.out.println("""
                ================================================
                       PREUZIMANJE SOFTVERSKIH KOMPONENTI
                ================================================
                """);
        help();
    }

    private static void help() {
        System.out.println("""
                Dostupne komande:
                  open <path>
                  download <organization> <name> <id> <targetDir> <conflictStrategy> <includeOptionalRoots>
                  help
                  exit
                """);
    }
}
