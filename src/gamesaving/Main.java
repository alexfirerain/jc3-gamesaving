package gamesaving;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    static Date currentTime = new Date();
    static SimpleDateFormat logDateFormat = new SimpleDateFormat("E d MMMM HH:mm:ss");
    static File[] dirsToCreate = {
            new File("game"),
            new File("game\\src"),
            new File("game\\res"),
            new File("game\\savegames"),
            new File("game\\temp"),
            new File("game\\src\\main"),
            new File("game\\src\\test"),
            new File("game\\res\\drawables"),
            new File("game\\res\\vectors"),
            new File("game\\res\\icons")
    };
    static File[] filesToCreate = {
            new File("game\\src\\main\\Main.java"),
            new File("game\\src\\main\\Utils.java"),
            new File("game\\temp\\temp.txt")
    };

    public static void main(String[] args) {
        System.out.println(createFiles() ?
                "Файлы и папки успешно созданы" :
                "При создании файлов возникли ошибки");

        // для удаления всего
//        deleteAll(dirsToCreate[0]);
    }

    private static boolean createFiles() {
        boolean success = true;
        StringBuilder log = new StringBuilder();

        for (File dir : dirsToCreate) {
            log.append(logDateFormat.format(currentTime.getTime())).append(" папка ").append(dir.getName());
            if (dir.exists()) {
                log.append(" уже существует и не будет создаваться\n");
                success = false;
            } else {
                try {
                    if (dir.mkdir()) {
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
//            System.out.println(log);
        }


        for (File file : filesToCreate) {
            log.append(logDateFormat.format(currentTime.getTime())).append(" файл ").append(file.getName());
            if (file.exists()) {
                log.append(" уже существует и не будет создаваться\n");
                success = false;
            } else {
                try {
                    if (file.createNewFile()) {
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
//            System.out.println(log);
        }

        try(FileWriter logger = new FileWriter("game\\temp\\temp.txt", true)) {
            logger.write(log.toString());
            logger.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(success ?
                "Файлы и папки созданы!" :
                "Ошибка создания файлов или папкок. Смотри подробности в temp\\temp.txt.");
        return success;
    }

    static void deleteAll(File toDelete) {
        if (!toDelete.exists()) return;
        if (toDelete.isDirectory()) {
            try {
                for (File file : toDelete.listFiles())
                    deleteAll(file);
            } catch (NullPointerException e) {
                System.out.println("Файл " + toDelete.getName() + " не найден");
                e.printStackTrace();
            }
        }
        toDelete.delete();
    }
}
