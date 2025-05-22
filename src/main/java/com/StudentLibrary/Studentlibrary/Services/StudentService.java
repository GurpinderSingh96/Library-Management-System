package com.StudentLibrary.Studentlibrary.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;

@Service
public class StudentService {

    Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    StudentRepository studentRepository;

    public void createStudent(Student student) {
        studentRepository.save(student);
        logger.info("New student created: {}", student);
    }

    public int updateStudent(Student student) {
        return studentRepository.updateStudentDetails(student);
    }

    public void deleteStudent(int id) {
        studentRepository.deleteCustom(id);
    }
}
