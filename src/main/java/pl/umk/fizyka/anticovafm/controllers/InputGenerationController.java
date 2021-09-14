package pl.umk.fizyka.anticovafm.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.umk.fizyka.anticovafm.services.InputGenerationService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class InputGenerationController {

    @Autowired
    private InputGenerationService inputGenerationService;

    @PostMapping("inputGeneration")
    public ResponseEntity inputGeneration(@RequestBody MultipartFile structure,
                                          @RequestBody MultipartFile psf,
                                          @RequestBody MultipartFile velocity,
                                          @RequestBody MultipartFile coordinates,
                                          @RequestBody MultipartFile extended,
                                          @RequestBody MultipartFile parameters,
                                          @RequestBody MultipartFile templateInput,
                                          @RequestBody MultipartFile templateRun,
                                          @RequestParam String selectConstrains,
                                          @RequestParam String selectPull) {

        try {
            Resource resource = inputGenerationService.generateInput(structure, psf, velocity, coordinates, extended,
                    parameters, templateInput, templateRun, selectConstrains, selectPull);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("applicaion/zip"))
                    .body(resource);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Server error");

        } catch (InterruptedException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Output generation was interrupted");
        }
    }
}
