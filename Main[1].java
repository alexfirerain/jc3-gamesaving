package gamesaving;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Date currentTime = new Date();
    private static final SimpleDateFormat logTimestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final String[] dirsToCreate = {
            "game",
            "game\\src",
            "game\\res",
            "game\\savegames",
            "game\\temp",
            "game\\src\\main",
            "game\\src\\test",
            "game\\res\\drawables",
            "game\\res\\vectors",
            "game\\res\\icons"
    };
    private static final String[] filesToCreate = {
            "game\\src\\main\\Main.java",
            "game\\src\\main\\Utils.java",
            "game\\temp\\temp.txt"
    };
    private static final Scanner input = new Scanner(System.in);

    private static String installDir = "";
    static GameState currentState = null;
    private static File tempDir,
                        savesDir,
                        logFile;

    public static void main(String[] args) {
        System.out.println("""
                            Здравствуйте!
                            Перед вами программа, демонстрирующая возможность сохранения состояний
                            имитации игры, которая, в свою очередь, является имитацией эволюции.
                            """);
        showMenu();
        boolean exit = false;
        while (!exit) {
            switch (input.nextLine()) {
                case "0", "exit" -> {
                    if (currentState == null || currentState.getSaveID() < 3) {
                        System.out.println("В демонстрации нужно создать не меньше трёх состояний!");
                    } else {
                        archiveSaves();
                        exit = true;
                    }
                }
                case "1", "install" -> {
                    System.out.println("""
                            Сейчас будут создаваться необходимые для игры папки и файлы.
                            Введите корректный путь для установки (или пустую строку оставить по умолчанию):
                            """);
                    installDir = input.nextLine();
                    File installPath = new File(installDir);
                    if (installPath.exists() && installPath.isDirectory()) {
                        System.out.println(createFoldersAndFiles(installDir, dirsToCreate, filesToCreate));
                    } else {
                        installDir = "";
                        System.out.println("Такой путь не найден, сброшен к умолчанию.");
                    }
                }
                case "2", "play" ->
                        currentState = setAState();
                case "3", "display" -> {
                    if (currentState == null) {
                        System.out.println("Ни одно состояние игры ещё не симулировано!");
                    } else {
                        System.out.println(currentState);;
                    }
                }
                case "4", "save" -> {
                    if (currentState == null) {
                        System.out.println("Прежде чем сохранять состояние игры, его нужно достичь!");
                    } else {
                        storeAState(savesDir.getAbsolutePath() +
                                "\\save" + currentState.getSaveID() + ".dat", currentState);
                    }
                }
                case "5", "load" ->
                        currentState = loadAState();
                case "6", "compress" ->
                        archiveSaves();
                case "8", "eliminate" ->
                        deleteAll();
                default ->
                        showMenu();
            }
        }
        System.out.println("""
                Спасибо за интерес к нашей игре!
                При повторном запуске демонстрации корректная работа не гарантируется и не ожидается.
                Рекомендуется сотворять файлы и папки заново, удалив созданные при прежних запусках
                вручную средствами ОС либо соответствующей коммандой из меню программы.""");
    }

    private static void archiveSaves() {

    }

    private static List<String> getStorageContent(File storage) {
        return Collections.stream(savesDir.listFiles()).filter(x -> x.getName().endsWith(".dat")).collect(Collectors.toList()));
    }

    private static GameState loadAState() {
        System.out.println("Присутствуют следующие сохранения:");
        for (File save : Objects.requireNonNull(savesDir.listFiles()))
            if (!save.isDirectory() && save.getName().endsWith(".dat"))
                System.out.println(save.getName());
        System.out.println("Какое сохранение следует загрузить -\n" +
                "введите число из имени файла:");
        int numberToLoad;
        try {
            numberToLoad = Integer.parseInt(input.nextLine());
        } catch (NumberFormatException n) {
            System.out.println("Неправильный ввод.");
            showMenu();
            return currentState;
        }
        GameState loadedState;
        try (ObjectInputStream stateReader = new ObjectInputStream(
                new FileInputStream(savesDir.getAbsolutePath() + "\\save" + numberToLoad + ".dat"))) {
            loadedState = (GameState) stateReader.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Сохранение с таким номером не найдено!");
            return currentState;
        } catch (IOException e) {
            System.out.println("Некая ошибка доступа!");
            return currentState;
        } catch (ClassNotFoundException e) {
            System.out.println("Не тот формат сохранения!");
            return currentState;
        }
        return loadedState;
    }

    private static GameState setAState() {
        GameState currentState = null;
        try {
            System.out.print("""
                                Сейчас представьте, что во всю кипит глобальная эволюционная стратегия,
                                и вы сохраняете текущее состояние игры, которое представлено как сумма параметров.
                                Сама механика игры ещё не написана, поэтому вам нужно ввести их чисто интуитивно.
                                                            
                                Итак, первый параметр - это номер домена, к которому принадлежит вид-протагонист.
                                Введите Integer от 0 до 8: """);
            int structureLevel = Integer.parseInt(input.nextLine());
            System.out.print("""
                                
                                Теперь генетическая сила твари. Число соответствует количеству
                                контролируемых мутаций на один эволюционный акт.
                                Введите Integer от 1 до нескольких тысяч: """);
            int geneticStrength= Integer.parseInt(input.nextLine());
            System.out.print("""
                                
                                Экологическая стратегия вида, существует 40 первоначальных вариантов,
                                но в процессе игры из их комбинаций и модификаций могут возникать ещё сотни новых.
                                Введите уместный Integer: """);
            int feedingType = Integer.parseInt(input.nextLine());
            System.out.print("""

                                Обобщённое количество ресурсов, доступных виду.
                                Введите Double от 1 до нескольких миллионов: """);
            double resources = Double.parseDouble(input.nextLine());
            System.out.print("""

                                Способность протагониста к агрессивному поведению.
                                Введите Double от 1 до нескольких десятков: """);
            double attack = Double.parseDouble(input.nextLine());
            System.out.print("""

                                Способность протагониста противостоять агрессивному поведению.
                                Введите Double от 1 до нескольких десятков: """);
            double defence = Double.parseDouble(input.nextLine());

            currentState = new GameState(structureLevel, geneticStrength, feedingType, resources, attack, defence);
            System.out.println( "Состояние игры №" + currentState.getSaveID() + " успешно достигнуто.\n" +
                                "Конечно, это лишь малая толика параметров, которые должны сохраняться,\n" +
                                "Но для демонстрации работы сохранения в самый раз.");
        } catch (Exception e) {
            System.out.println("""
                                Возникла ошибка (скорее всего, неправильный ввод числа).
                                Новое состояние игры не было достигнуто.
                                """);
            showMenu();
        }
        return currentState;
    }

    private static void storeAState(String placeWhereToSave, GameState stateToSave) {
        File save = new File(placeWhereToSave);
        if (save.exists())


    }

    private static void showMenu() {
        System.out.println("""

                    Комманды:
                    1 / install = имитировать установку
                    2 / play = имитировать игровой процесс
                    3 / display = отразить текущее состояние 
                    4 / save = сохраненить текущее состояние
                    5 / load = загрузить состояние из сохранёнки
                    6 / zip = архивировать сохранёнки
                    8 / eliminate = полностью удалить
                    0 / exit = выход""");
    }

    private static String createFoldersAndFiles(String installPath, String[] dirsToCreate, String[] filesToCreate) {
        boolean success = true;
        StringBuilder log = new StringBuilder();

        for (String dirPath : dirsToCreate) {
            File newDir = new File(installPath + dirPath);
            log.append(logTimestamp.format(currentTime.getTime())).
                    append(" папка ").append(newDir.getAbsolutePath()).append("\\");
            if (newDir.exists()) {
                log.append(" уже существует и не будет создаваться\n");
                success = false;
            } else {
                try {
                    if (newDir.mkdir()) {
                        log.append(" создана\n");
                    } else {
                        log.append(" не создана\n");
                        success = false;
                    }
                } catch (SecurityException e) {
                    log.append(" -> возникло исключение: ").append(e.getMessage()).append("\n");
                    success = false;
                }
            }
        }
        for (String filePath : filesToCreate) {
            File newFile = new File(installPath + filePath);
            log.append(logTimestamp.format(currentTime.getTime())).
                    append(" файл ").append(newFile.getAbsolutePath());
            if (newFile.exists()) {
                log.append(" уже существует и не будет создаваться\n");
                success = false;
            } else {
                try {
                    if (newFile.createNewFile()) {
                        log.append(" создан\n");
                    } else {
                        log.append(" не создан\n");
                        success = false;
                    }
                } catch (SecurityException | IOException e) {
                    log.append(" -> возникло исключение: ").append(e.getMessage()).append("\n");
                    success = false;
                }
            }
        }
        log.append("\n");

        tempDir = new File(installDir + dirsToCreate[4]);
        savesDir = new File(installDir + dirsToCreate[3]);
        logFile = new File(installDir + filesToCreate[2]);

        try(FileWriter logger = new FileWriter(logFile, true)) {
            logger.write(log.toString());
            logger.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success ?
                "Все файлы и папки успешно созданы!" :
                "Ошибка создания файлов или папкок.\n" +
                "Смотри подробности в " + logFile.getPath() + ".";
    }

    static void deleteAll() {
        deleteDownwards(installDir + dirsToCreate[0]);
    }

    static void deleteDownwards(String nameToDelete) {
        if (nameToDelete == null) {
            System.out.println("Нечего удалять.");
            return;
        }
        File toDelete = new File(nameToDelete);
        if (!toDelete.exists()) {
            System.out.println(nameToDelete + " - нельзя удалить то, чего нет.");
            return;
        }
        boolean isDir = toDelete.isDirectory();
        if (isDir) {
            try {
                for (File file : Objects.requireNonNull(toDelete.listFiles()))
                    deleteDownwards(file.getAbsolutePath());
            } catch (NullPointerException e) {
                System.out.println("Файл " + toDelete.getPath() + " не найден");
            }
        }
        if(toDelete.delete()) {
            System.out.println(toDelete.getAbsolutePath() + (
                    isDir ? "\\ и всё низлежащее удалено" : " удалён"));
        } else
            System.out.println(toDelete.getAbsolutePath() +
                    " не может быть " + (isDir ? " удалена." : " удалён."));
    }
}
