package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.SubjectDTO;
import com.examination.online_examination_server.entity.Subject;
import com.examination.online_examination_server.repository.SubjectRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository; // Changed from ClassRepository to SubjectRepository

    @Autowired
    private ModelMapper modelMapper;

    // Save a new subject
    public String SaveSubject(SubjectDTO subjectDTO) {
        if (subjectRepository.existsById(subjectDTO.getId())) {
            return VarList.RES_DUPLICATE; // Return a duplicate response if subject already exists
        } else {
            subjectRepository.save(modelMapper.map(subjectDTO, Subject.class)); // Save the subject entity
            return VarList.RES_SUCCESS;
        }
    }

    // Retrieve all subjects
    public List<SubjectDTO> GetAllSubjects() {
        List<Subject> subjectList = subjectRepository.findAll(); // Fetch all subjects
        return modelMapper.map(subjectList, new TypeToken<List<SubjectDTO>>(){}.getType()); // Convert to DTO
    }

    // Update an existing subject
    public String UpdateSubject(SubjectDTO subjectDTO) {
        if (subjectRepository.existsById(subjectDTO.getId())) {
            subjectRepository.save(modelMapper.map(subjectDTO, Subject.class)); // Update subject if it exists
            return VarList.RES_SUCCESS;
        } else {
            return VarList.RES_NO_DATE_FOUND; // Return a "no data found" response if subject doesn't exist
        }
    }

    // Delete a subject by its ID
//    public String deleteSubject(int id) {
//        if (subjectRepository.existsById(id)) {
//            subjectRepository.deleteById(id); // Delete subject if it exists
//            return VarList.RES_SUCCESS;
//        } else {
//            return VarList.RES_NO_DATE_FOUND; // Return a "no data found" response if subject doesn't exist
//        }
//    }


    // Soft delete a subject
    public String deleteSubject(int id) {
        Optional<Subject> subjectOptional = subjectRepository.findById(id);
        if (subjectOptional.isPresent()) {
            Subject subject = subjectOptional.get();
            if (!subject.isDeleted()) {
                subjectRepository.deleteById(id); // Trigger the soft delete SQL
                return VarList.RES_SUCCESS; // Success code
            } else {
                return VarList.RES_ALREADY_DELETED; // Return a "no data found" response if subject doesn't exist
            }
        } else {
            return VarList.RES_NO_DATE_FOUND; // Return a "no data found" response if subject doesn't exist
        }
    }

    // Get all non-deleted subjects
//    public List<Subject> getAllSubjects() {
//        return subjectRepository.findAll(); // Automatically excludes deleted subjects
//    }

    // Get all soft-deleted subjects
//    public List<Subject> getAllDeletedSubjects() {
//        return subjectRepository.findAllDeletedSubjects(); // Fetch soft-deleted subjects
//    }

    // Search for a specific subject by its ID
    public SubjectDTO SearchSubject(int id) {
        if (subjectRepository.existsById(id)) {
            Subject subject = subjectRepository.findById(id).orElse(null); // Fetch subject by ID
            return modelMapper.map(subject, SubjectDTO.class); // Map to DTO
        } else {
            return null; // Return null if subject doesn't exist
        }
    }
}
