package com.example.BookingApplication.Service;

import com.example.BookingApplication.Entity.Studio;
import com.example.BookingApplication.Repositories.StudioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudioService {

    @Autowired
    private StudioRepository studioRepository;

    public void PostStudios(Studio newstudio) {
        studioRepository.save(newstudio);
    }

    public List<Studio> GetallStudios() {
        List<Studio> studioList = studioRepository.findAll();
        return studioList;
    }
}
