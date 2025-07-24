package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.GradeDTO;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Grade;
import com.examination.online_examination_server.repository.GradeRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeService {
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private ModelMapper modelMapper;

    public String saveGrade(GradeDTO gradeDTO) {
        if (gradeRepository.existsById(gradeDTO.getId())) {
            return VarList.RES_DUPLICATE;
        }else {
            gradeRepository.save(modelMapper.map(gradeDTO, Grade.class));
            return VarList.RES_SUCCESS;
        }
    }

    public List<GradeDTO> getAllGrades() {
        List<Grade> gradeList = gradeRepository.findAll();
        return modelMapper.map(gradeList, new TypeToken<List<GradeDTO>>(){}.getType());
    }

    public String updateGrade(GradeDTO gradeDTO) {
        if (gradeRepository.existsById(gradeDTO.getId())){
            gradeRepository.save(modelMapper.map(gradeDTO, Grade.class));
            return VarList.RES_SUCCESS;
        }else {
            return VarList.RES_NO_DATE_FOUND;
        }
    }

    public String deleteGrade(int id){
        if (gradeRepository.existsById(id)){
            gradeRepository.deleteById(id);
            return VarList.RES_SUCCESS;
        }else {
            return VarList.RES_NO_DATE_FOUND;
        }
    }


    // Soft delete a Grade
//    public String deleteGrade(int id) {
//        Optional<Grade> gradeOptional = gradeRepository.findById(id);
//        if (gradeOptional.isPresent()) {
//            Grade grd = gradeOptional.get();
//            if (!grd.isDeleted()) {
//                gradeRepository.deleteById(id); // Trigger the soft delete SQL
//                return VarList.RES_SUCCESS; // Success code
//            } else {
//                return VarList.RES_ALREADY_DELETED; // Return a "no data found" response if subject doesn't exist
//            }
//        } else {
//            return VarList.RES_NO_DATE_FOUND; // Return a "no data found" response if subject doesn't exist
//        }
//    }


    // Get all non-deleted subjects
//    public List<Subject> getAllSubjects() {
//        return subjectRepository.findAll(); // Automatically excludes deleted subjects
//    }

    // Get all soft-deleted subjects
//    public List<Subject> getAllDeletedSubjects() {
//        return subjectRepository.findAllDeletedSubjects(); // Fetch soft-deleted subjects
//    }

    public GradeDTO getGradeById(int id){
        if (gradeRepository.existsById(id)){
            Grade grd = gradeRepository.findById(id).orElse(null);
            return modelMapper.map(grd, GradeDTO.class);
        }else {
            return null;
        }
    }
}
