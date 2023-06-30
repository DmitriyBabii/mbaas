package com.mbaas.controllers;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.files.FileInfo;
import com.mbaas.models.OpenFile;
import com.mbaas.services.mappers.OpenFileMapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my-files")
@AllArgsConstructor
public class FileController {
    private final OpenFileMapper openFileMapper;

    @GetMapping
    public ModelAndView getUserFiles(ModelAndView modelAndView) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        List<FileInfo> files = Backendless.Files.listing(user.getProperty("name").toString())
                .stream()
                .filter(e -> !e.getName().equals("shared"))
                .collect(Collectors.toList());
        modelAndView.addObject("files", files);
        modelAndView.setViewName("files.html");
        return modelAndView;
    }

    @GetMapping("/shared")
    public ModelAndView getUserSharedFiles(ModelAndView modelAndView) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        String username = user.getProperty("name").toString();
        List<FileInfo> files = Backendless.Files.listing(String.format("%s/shared", username));
        List<OpenFile> openFiles = files.stream()
                .map(openFileMapper)
                .collect(Collectors.toList());
        modelAndView.addObject("files", openFiles);
        modelAndView.setViewName("shared-files.html");
        return modelAndView;
    }

    @PostMapping("/upload")
    public ModelAndView uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ModelAndView("redirect:/my-files");
        }
        try {
            String fileName = file.getOriginalFilename();
            byte[] fileData = file.getBytes();
            Backendless.Files.saveFile((String) Backendless.UserService.CurrentUser().getProperty("name"), fileName, fileData, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/my-files");
    }

    @PostMapping("/delete/{name}")
    public ModelAndView deleteFile(@PathVariable("name") String name) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        FileInfo file = getFileInfoByName(user.getProperty("name").toString(), name);
        if (file == null) {
            return new ModelAndView("redirect:/my-files");
        }
        Backendless.Files.remove(user.getProperty("name") + "/" + file.getName());
        return new ModelAndView("redirect:/my-files");
    }

    @PostMapping("/share/{name}")
    public ModelAndView shareFile(@PathVariable("name") String name, @RequestParam("receiver") String receiver) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        FileInfo file = getFileInfoByName(user.getProperty("name").toString(), name);
        if (file == null) {
            return new ModelAndView("redirect:/my-files");
        }
        boolean receiverExists = Backendless.Files.listing("/").stream().anyMatch(e -> e.getName().equals(receiver));
        if (receiverExists) {
            Backendless.Files.saveFile(receiver + "/shared", file.getName(), file.getPublicUrl().getBytes(), true);
        }
        return new ModelAndView("redirect:/my-files");
    }

    private FileInfo getFileInfoByName(String userName, String fileName) {
        return Backendless.Files.listing(userName)
                .stream()
                .filter(e -> e.getName().equals(fileName))
                .findFirst()
                .orElse(null);
    }
}
