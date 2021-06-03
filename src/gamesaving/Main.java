package src.gamesaving;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    // служебная инициализация
    private static final Date currentTime = new Date();
    private static final SimpleDateFormat logTimestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final Scanner input = new Scanner(System.in);

    // список файлов
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

    // константы и переменные приложения
    private static String installDir = "";
    private static File savesDir,
                        logFile,
                        archiveFile;
    static GameState currentState = null;

    // сценарий исполнения
    public static void main(String[] args) {
        System.out.println("""
                            
                            Здравствуйте!
                            Перед вами программа, демонстрирующая возможность сохранения состояний
                            имитации игры, которая, в свою очередь, является имитацией эволюции.""");
        showMenu();
        boolean exit = false;
        while (!exit) {
            switch (input.nextLine()) {
                case "0", "exit" -> {
                    if (GameState.getIdCounter() >= 3 ||
                            savesDir != null && getStorageContent(savesDir, ".dat").size() >= 3) {
                        archiveSaves(savesDir, getStorageContent(savesDir, ".dat"));
                        exit = true;
                    } else {
                        System.out.println("В демонстрации нужно создать не меньше трёх состояний!");
                    }
                }
                case "1", "install" -> {
                    System.out.println("""
                            Сейчас будут создаваться необходимые для игры папки и файлы.
                            Введите корректный путь для установки (или пустую строку оставить по умолчанию):
                            """);
                    installDir = input.nextLine();                                  // здесь не очень понятно!
                    if (isAValidDirectory(installDir) || installDir.equals("")) {
                        System.out.println(
                                createFoldersAndFiles(installDir, dirsToCreate, filesToCreate)
                        );
                    } else {
                        installDir = "";
                        System.out.println("Такой путь не найден, сброшен к умолчанию.");
                    }
                }
                case "2", "play" -> {
                    if (logFile == null || !logFile.exists()) {
                        System.out.println("Кажется, игра не установлена!");
                    } else {
                        currentState = setAState();
                    }
                }
                case "3", "display" -> {
                    if (currentState == null) {
                        System.out.println("Ни одно состояние игры ещё ни симулировано, ни загружено!");
                    } else {
                        System.out.println(currentState);
                    }
                }
                case "4", "save" -> {
                    if (currentState == null || savesDir == null) {
                        System.out.println("Прежде чем сохранять состояние игры, его нужно достичь!");
                    } else {
                        storeAState(savesDir.getAbsolutePath() +
                                "\\save" + currentState.getSaveID() + ".dat", currentState);
                    }
                }
                case "5", "load" -> {
                    if (savesDir == null) {
                        System.out.println("Игра не инициализирована! Повторите установку.");
                    } else {
                        currentState = loadAState();
                    }
                }
                case "6", "zip" -> {
                    if (savesDir == null) {
                        System.out.println("Игра не инициализирована! Повторите установку.");
                    } else {
                        archiveSaves(savesDir, getStorageContent(savesDir, ".dat"));
                    }
                }
                case "7", "unzip" -> {
                    if (archiveFile == null) {
                        System.out.println("Игра не инициализирована! Повторите установку.");
                    } else {
                        extractSaves(archiveFile, savesDir);
                    }
                }
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

    // нужности основного взаимодействия:
    private static void showMenu() {
        System.out.println("""

                    Комманды:
                    1 / install     = имитировать установку
                    2 / play        = имитировать игровой процесс
                    3 / display     = отразить текущее состояние 
                    4 / save        = сохраненить текущее состояние
                    5 / load        = загрузить состояние из сохранёнки
                    6 / zip         = архивировать сохранёнки
                    7 / unzip       = разархивировать сохранёнки
                    8 / eliminate   = полностью удалить
                      <пусто>       = показать меню
                    0 / exit        = выход""");
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

        archiveFile = new File(installDir + dirsToCreate[3] + "\\saves.zip");
        savesDir = new File(installDir + dirsToCreate[3]);
        logFile = new File(installDir + filesToCreate[2]);

        try(FileWriter logger = new FileWriter(logFile, true)) {
            logger.write(log.toString());
            logger.flush();
        } catch (IOException e) {
            System.out.println("Что-то лог не пишется! ->" + e.getMessage());
        }

        return success ?
                "Все файлы и папки успешно созданы!" :
                "Ошибка создания файлов или папкок.\n" +
                "Смотри подробности в " + logFile.getPath() + ".";
    }

    private static GameState setAState() {
        GameState newState;
        try {
            System.out.print("""
                                Сейчас представьте, что во всю кипит глобальная эволюционная стратегия,
                                и вы сохраняете текущее состояние игры, которое представлено как сумма параметров.
                                Сама механика игры ещё не написана, поэтому вам нужно ввести их чисто интуитивно.
                                                            
                                Итак, первый параметр - это номер домена, к которому принадлежит вид-протагонист.
                                Введите Integer от 0 до 8:\040""");
            int structureLevel = Integer.parseInt(input.nextLine());
            System.out.print("""
                                
                                Теперь генетическая сила твари. Число соответствует количеству
                                контролируемых мутаций на один эволюционный акт.
                                Введите Integer от 1 до нескольких тысяч:\040""");
            int geneticStrength= Integer.parseInt(input.nextLine());
            System.out.print("""
                                
                                Экологическая стратегия вида, существует 40 первоначальных вариантов,
                                но в процессе игры из их комбинаций и модификаций могут возникать ещё сотни новых.
                                Введите уместный Integer:\040""");
            int feedingType = Integer.parseInt(input.nextLine());
            System.out.print("""

                                Обобщённое количество ресурсов, доступных виду.
                                Введите Double от 1 до нескольких миллионов:\040""");
            double resources = Double.parseDouble(input.nextLine());
            System.out.print("""

                                Способность протагониста к агрессивному поведению.
                                Введите Double от 1 до нескольких десятков:\040""");
            double attack = Double.parseDouble(input.nextLine());
            System.out.print("""

                                Способность протагониста противостоять агрессивному поведению.
                                Введите Double от 1 до нескольких десятков:\040""");
            double defence = Double.parseDouble(input.nextLine());

            newState = new GameState(structureLevel, geneticStrength, feedingType, resources, attack, defence);
            System.out.printf("""
                                Состояние игры №%d успешно достигнуто.
                                
                                Конечно, это лишь малая толика параметров, которые должны сохраняться,
                                но для демонстрации работы сохранения в самый раз.
                                Не забудьте сохранить это состояние!%n""", newState.getSaveID());
        } catch (Exception e) {
            System.out.println("""
                                Возникла ошибка (скорее всего, неправильный ввод числа).
                                Новое состояние игры не было достигнуто.
                                """);
            return currentState;
        }
        return newState;
    }

    private static void storeAState(String fileToStore, GameState stateToSave) {
        File save = new File(fileToStore);
        if (save.exists()) {
            System.out.println("Это состояние, кажется, уже сохранено!");
            return;
        }
        try (ObjectOutputStream saveOutput = new ObjectOutputStream(
                new FileOutputStream(fileToStore))) {
//            if (!save.createNewFile()) {
//                System.out.println("Это состояние, кажется, уже сохранено!");
//                saveOutput.close();
//                return;
//            }
                saveOutput.writeObject(stateToSave);
        } catch (FileNotFoundException e) {
            System.out.println("Не нашлось файла!");
            return;
        } catch (IOException e) {
            System.out.println("Нет доступа к хранилищу!");
            return;
        }
        System.out.printf("Состояние №%d сохранено в %s%n", stateToSave.getSaveID(), fileToStore);
    }

    private static GameState loadAState() {
        List<String> saves = getStorageContent(savesDir, ".dat");
        if (saves == null || saves.size() == 0) {
            System.out.println("Сохранения не обнаружены!");
            return currentState;
        }
        System.out.println("Присутствуют следующие сохранения:");
        saves.forEach(System.out::println);
        System.out.println("Какое сохранение следует загрузить -\n" +
                "введите число из имени файла:");
        int numberToLoad;
        try {
            numberToLoad = Integer.parseInt(input.nextLine());
        } catch (NumberFormatException n) {
            System.out.println("Неправильный ввод.");
            return currentState;
        }
        GameState loadedState;
        try (ObjectInputStream stateReader = new ObjectInputStream(
                new FileInputStream(savesDir.getAbsolutePath() + "\\save" + numberToLoad + ".dat"))) {
            loadedState = (GameState) stateReader.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Сохранение с таким номером не найдено!");
            return currentState;
        } catch (ClassNotFoundException e) {
            System.out.println("Не тот формат сохранения!");
            return currentState;
        } catch (IOException e) {
            System.out.println("Некая ошибка доступа!");
            return currentState;
        }
        return loadedState;
    }

    private static void archiveSaves(File destinationFolder, List<String> fileList) {
        if (archiveFile == null) {
            System.out.println("Игра, кажется, не инициализирована! Повторите установку.");
            return;
        }
        if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
            System.out.println("Нет контакта с папкой сохранения, архивации не будет!");
            return;
        }
        if (fileList == null || fileList.size() == 0) {
            System.out.println("Нечего сохранять, архивации не будет!");
            return;
        }
//        try {
//            if (archiveFile.delete())
//                if(!archiveFile.createNewFile())
//                    throw new SecurityException("Что-то не так!");
//        } catch (SecurityException | IOException e) {
//            System.out.println("Не переписать старый архив! <-" + e.getMessage());
//            return;
//        }
        boolean archived = false;
        try (ZipOutputStream archiver = new ZipOutputStream(
                new FileOutputStream(destinationFolder + "\\" + archiveFile.getName())) ){
            for (String name : Objects.requireNonNull(fileList)) {
                archiver.putNextEntry(new ZipEntry(name));
                FileInputStream reading = new FileInputStream(destinationFolder + "\\" + name); // File + String !?!
                byte[] buffer = new byte[reading.available()];
                reading.read(buffer);
                archiver.write(buffer);
                archiver.closeEntry();
                reading.close();
            }
            System.out.println("Архив записан.");
            archived = true;
        } catch (IOException e) {
            System.out.println("Ошибка работы хранилища! <-" + e.getMessage());
        }

        if (archived) {
            try {
                for (String file : Objects.requireNonNull(fileList)) {
                    File d = new File(destinationFolder + "\\" + file);
                    System.out.println(file +
                            (new File(destinationFolder + "\\" + file).delete() ?
                                    " удалён" : " не удалён")
                    );
                }
            } catch (SecurityException e) {
                System.out.println("Что-то из сохранений не удалить! <-" + e.getMessage());
            }
        }
    }

    private static void extractSaves(File archiveFile, File destination) {
        if (!archiveFile.exists()) {
            System.out.println("Архив сохранений не найден!");
            return;
        }
        boolean extracted = false;
        try (ZipInputStream decompressor = new ZipInputStream(new FileInputStream(archiveFile))) {
            ZipEntry entry;
            while ((entry = decompressor.getNextEntry()) != null) {
                FileOutputStream writing = new FileOutputStream(
                        destination.getAbsolutePath() + "\\" + entry.getName());
                for (int c = decompressor.read(); c != -1; c = decompressor.read())
                    writing.write(c);
                writing.flush();
                decompressor.closeEntry();
                writing.close();
            }
            extracted = true;
        } catch (IOException e) {
            System.out.println("Проблемы с распаковкой! <-" + e.getMessage());
        }
        if (extracted)
            archiveFile.delete();
    }

    private static void deleteAll() {
        deleteDownwards(installDir + dirsToCreate[0]);
    }

    // служебные нужности
    private static boolean isAValidDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.exists() && directory.isDirectory();
    }
//    private static List<String> getStorageContent(File storage) {
////        if (storage.listFiles() == null) return null;
//        return Arrays.stream(Objects.requireNonNull(storage.listFiles())).
//                filter(x -> !x.isDirectory()).
//                map(File::getName).
//                collect(Collectors.toList());
//    }
    private static List<String> getStorageContent(File storage, String ext) {
//        if (storage.listFiles() == null) return null;
        return Arrays.stream(Objects.requireNonNull(storage.listFiles())).
                filter(x -> !x.isDirectory()).
                map(File::getName).
                filter(name -> name.endsWith(ext)).
                collect(Collectors.toList());
    }
//    private static List<File> getFileList(File storage, String ext) {
//        if (storage.listFiles() == null) return null;
//        return Arrays.stream(Objects.requireNonNull(storage.listFiles())).
//                filter(x -> !x.isDirectory()).
//                filter(name -> name.getName().endsWith(ext)).
//                collect(Collectors.toList());
//    }
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
