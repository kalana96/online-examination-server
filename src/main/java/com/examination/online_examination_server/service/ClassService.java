package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.ClassDTO;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Subject;
import com.examination.online_examination_server.repository.ClassRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClassService {
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private ModelMapper modelMapper;

    public String SaveClass(ClassDTO classDTO) {
        if (classRepository.existsById(classDTO.getId())) {
            return VarList.RES_DUPLICATE;
        } else {
            classRepository.save(modelMapper.map(classDTO, Class.class));
            return VarList.RES_SUCCESS;
        }
    }


    public List<ClassDTO> GetAllClass() {
        List<Class> empList = classRepository.findAll();
        return modelMapper.map(empList, new TypeToken<List<ClassDTO>>() {
        }.getType());
    }

    public String UpdateClass(ClassDTO classDTO) {
        if (classRepository.existsById(classDTO.getId())) {
            classRepository.save(modelMapper.map(classDTO, Class.class));
            return VarList.RES_SUCCESS;
        } else {
            return VarList.RES_NO_DATE_FOUND;
        }
    }

//    public String deleteClass(int id){
//        if (classRepository.existsById(id)){
//            classRepository.deleteById(id);
//            return VarList.RES_SUCCESS;
//        }else {
//            return VarList.RES_NO_DATE_FOUND;
//        }
//    }


    // Soft delete a subject
    public String deleteClass(int id) {
        Optional<Class> classOptional = classRepository.findById(id);
        if (classOptional.isPresent()) {
            Class cl = classOptional.get();
            if (!cl.isDeleted()) {
                classRepository.deleteById(id); // Trigger the soft delete SQL
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

    public ClassDTO SearchClass(int id) {
        if (classRepository.existsById(id)) {
            Class cl = classRepository.findById(id).orElse(null);
            return modelMapper.map(cl, ClassDTO.class);
        } else {
            return null;
        }
    }

}
