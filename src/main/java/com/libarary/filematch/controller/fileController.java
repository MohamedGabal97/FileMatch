package com.libarary.filematch.controller;

import com.libarary.filematch.service.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/file")
public class fileController {


    @Autowired
    private IFileService fileService;

    @GetMapping("/compare")
    public Map<String, Double> compareFiles() {
        Map<String, Double> score = fileService.compareFiles();
        log.info("\n----------------------------------------------\n score: {} \n----------------------------------------------", score);
        return score;
    }


}
