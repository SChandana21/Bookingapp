package com.example.BookingApplication.Controllers;

import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Service.StudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/studio")
public class AdminController {

    @Autowired
    private StudioService studioService;

    @PostMapping("/add")
    public ResponseEntity<?> CreatenewStudio(@RequestBody Studio Newstudio) {
    try {
        studioService.PostStudios(Newstudio);
        return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    }

    @GetMapping("/List")
    public ResponseEntity<?> GetallStudios() {
        try {
            List<Studio> studios = studioService.GetallStudios();
            return new ResponseEntity<>(studios, HttpStatus.FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
