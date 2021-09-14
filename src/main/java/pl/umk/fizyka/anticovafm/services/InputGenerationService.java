package pl.umk.fizyka.anticovafm.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class InputGenerationService {

    public Resource generateInput(MultipartFile structure,
                                  MultipartFile psf,
                                  MultipartFile velocity,
                                  MultipartFile coordinates,
                                  MultipartFile extended,
                                  MultipartFile parameters,
                                  MultipartFile templateInput,
                                  MultipartFile templateRun,
                                  String selectConstrains,
                                  String selectPull) throws IOException, InterruptedException
    {
        // Create temporary directory
        Path tempDirectory = Files.createTempDirectory("input-");

        // Save files to temporary directory
        structure.transferTo(tempDirectory.resolve("structure"));
        psf.transferTo(tempDirectory.resolve("psf"));
        velocity.transferTo(tempDirectory.resolve("velocity"));
        coordinates.transferTo(tempDirectory.resolve("coordinates"));
        extended.transferTo(tempDirectory.resolve("extended"));
        parameters.transferTo(tempDirectory.resolve("parameters"));
        templateInput.transferTo(tempDirectory.resolve("templateInput"));
        templateRun.transferTo(tempDirectory.resolve("templateRun"));

        // Call python converter
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(tempDirectory.toFile());
        processBuilder.command("AxCalc.py", "structure", "psf", "velocity", "coordinates",
                "extended", "parameters", "templateInput", "templateRun", selectConstrains,
                selectPull);
        Process process = processBuilder.start();
        while (process.isAlive()) {
        }

        // Compress the results
        FileOutputStream fos = new FileOutputStream(tempDirectory.resolve("output.zip").toString());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = tempDirectory.resolve("output").toFile();
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();

        // Read compressed file to memory
        byte[] array = Files.readAllBytes(tempDirectory.resolve("output.zip"));
        ByteArrayResource resource = new ByteArrayResource(array);

        // Remove temporary directory
        FileSystemUtils.deleteRecursively(tempDirectory);

        // Returns
        return resource;
    }


    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
