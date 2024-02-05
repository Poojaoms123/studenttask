package com.example.demo11.Service.ServiceImpl;

import com.example.demo11.Model.Response.pageDTO;
import com.example.demo11.Model.SaveRequest.SaveStudentRequest;
import com.example.demo11.Model.Student;
import com.example.demo11.Repository.StudentRepository;
import com.example.demo11.Service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private  OTPservice otPservice;



    @Override
    public String saveOrUpdateStudent(SaveStudentRequest saveStudentRequest) throws Exception {
        if (studentRepository.existsById(saveStudentRequest.getStudentId())) {
            List<Long> ids = new ArrayList<>();
            ids.add(saveStudentRequest.getStudentId());
            Student student = studentRepository.findById(saveStudentRequest.getStudentId()).get();
            student.setStudentName(saveStudentRequest.getStudentName());

            if (studentRepository.existsByStudentEmailAndStudentIdNotIn(saveStudentRequest.getStudentEmail(), ids)) {
                throw new Exception("Email already exits");

            } else {
                student.setStudentEmail(saveStudentRequest.getStudentEmail());
            }
            if (studentRepository.existsByStudentMobileNoAndStudentIdNotIn(saveStudentRequest.getStudentMobileNo(), ids)) {
                throw new Exception("MobileNo already exists");
            } else {
                student.setStudentMobileNo(saveStudentRequest.getStudentMobileNo());
            }
            student.setStudentIsDeleted(false);
            student.setStudentIsavtive(true);
            studentRepository.save(student);
            int otp = otPservice.generateOTP(saveStudentRequest.getStudentEmail());
            this.sendEmail("gitanjalichittar.oms@gmail.com","Application","Hi Pooja"+otp);
            return "update Sucessfully";
        } else {
            Student student = new Student();
            student.setStudentName(saveStudentRequest.getStudentName());
            if (studentRepository.existsByStudentEmail(saveStudentRequest.getStudentEmail())) {
                throw new Exception("Email already exists");
            } else {
                student.setStudentEmail((saveStudentRequest.getStudentEmail()));
            }
            if (studentRepository.existsByStudentMobileNo(saveStudentRequest.getStudentMobileNo())) {
                throw new Exception("Mobile No aa exists");
            } else {
                student.setStudentMobileNo(saveStudentRequest.getStudentMobileNo());
            }
            student.setStudentIsDeleted(false);
            student.setStudentIsavtive(true);
            studentRepository.save(student);
            int otp = otPservice.generateOTP(saveStudentRequest.getStudentEmail());
            this.sendEmail("gitanjalichittar.oms@gmail.com","Application","Hi pooja"+otp);
            return "save sucessfully";

        }
    }




    public void sendEmail(String toEmail, String subject , String body){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        javaMailSender.send(mailMessage);

    }

    @Override
    public Object getById(Long studentId) throws Exception {
        if (studentRepository.existsById(studentId)) {
            Student student = studentRepository.findById(studentId).get();
            return student;
        } else {
            throw new Exception("student not found");
        }
    }

    @Override
    public Object deleteById(Long studentId) throws Exception {
        if (studentRepository.existsById(studentId)) {
         /*   Student student=studentRepository.findById(studentId).get();
            student.setStudentIsDeleted(false);
            studentRepository.save(student);
            return "Deleted sucessfully";*/
            studentRepository.deleteByStudentId(studentId);
            return "Deleted sucessfully";
        } else {
            throw new Exception("Student not found");
        }
    }

    @Override
    public Object changestatus(Long studentId) throws Exception {
        if (studentRepository.existsById(studentId)) {
            Student student = studentRepository.findByStudentId(studentId);
            if (student.getStudentIsavtive()) {
                student.setStudentIsavtive(true);
                return "student active";
            } else {
                student.setStudentIsavtive(false);
                return "student inactive";
            }
        } else {
            throw new Exception("student not exits");
        }
    }

    @Override
    public Object getAllByDeleted(String startDate, String endDate, Pageable pageable) {
        Page<Student> students;
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            LocalDate startDate1 = LocalDate.parse(startDate);
            LocalDate endDate1 = LocalDate.parse(endDate);

            LocalDateTime startDateTime = LocalDateTime.of(startDate1, LocalTime.of(0, 0));
            LocalDateTime endDateTime = LocalDateTime.of(endDate1, LocalTime.of(23, 59));
            students = studentRepository.getByAllDateFilter(startDateTime, endDateTime, pageable);

        } else {
            students = studentRepository.getAllByDeleted(pageable);
        }
        return new pageDTO(students.getContent(), students.getTotalElements(), students.getNumber(), students.getTotalPages());
    }

    @Override
    public Object getAllByDeleted(String startDate, String endDate, String studentName, String studentEmail, String studentMobileNo, Pageable pageable) {
        Page<Student> students = null;
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            LocalDate startDate1 = LocalDate.parse(startDate);
            LocalDate endDate2 = LocalDate.parse(endDate);
            LocalDateTime startDateTime = LocalDateTime.of(startDate1, LocalTime.of(0, 0));
             LocalDateTime endDateTime = LocalDateTime.of(endDate2, LocalTime.of(23, 59));

            if (StringUtils.isNotBlank(studentName) || StringUtils.isNotBlank(studentEmail) || StringUtils.isNotBlank(studentMobileNo)) {
                if (StringUtils.isNotBlank(studentName)) {
                    studentName = studentName.toLowerCase();
                } else {
                    studentName = null;
                }
                if (StringUtils.isNotBlank(studentEmail)) {
                    studentEmail = studentEmail.toLowerCase();
                } else {
                    studentEmail = null;
                }
                if (StringUtils.isNotBlank(studentMobileNo)) {
                    studentMobileNo = null;
                }
                students = studentRepository.getAllByStudentNameByDateFilter(studentName, studentEmail, studentMobileNo, startDateTime, endDateTime, pageable);
            } else {
                students = studentRepository.getByAllDateFilter(startDateTime,endDateTime, pageable);
            }
        } else {
            if (StringUtils.isNotBlank(studentName) || StringUtils.isNotBlank(studentEmail) || StringUtils.isNotBlank(studentMobileNo)) {
                if (StringUtils.isNotBlank(studentName)) {
                    studentName = studentName.toLowerCase();
                } else {
                    studentName = null;
                }
                if (StringUtils.isNotBlank(studentEmail)) {
                    studentEmail = studentEmail.toLowerCase();
                } else {
                    studentEmail = null;
                }
                if (StringUtils.isNotBlank(studentMobileNo)) {
                    studentMobileNo = null;
                    students = studentRepository.getAllByStudentIdDeleted(studentName, studentEmail, studentMobileNo, pageable);
                } else {
                    students = studentRepository.getAllByDeleted(pageable);
                }
            }
        }
        return new pageDTO(students.getContent(), students.getTotalElements(), students.getNumber(), students.getTotalPages());

    }

/*
    @Override
    public Object getAllByDeleted(String studentName, Pageable pageable) {
        Page<Student> students;
        if (studentName != null && !studentName.isEmpty()) {
            students = studentRepository.findAllByStudentName(studentName, pageable);
        } else {
            students = studentRepository.getAllByDeleted(pageable);
        }
        return new pageDTO(students.getContent(), students.getTotalElements(), students.getNumber(), students.getTotalPages());
    }*/

 /*   @Override
    public Object getAllByDeleted(String studentName, String studentEmail, String studentMobileNo, Pageable pageable) {
        Page<Student> students;
        if (StringUtils.isNotBlank(studentName)) {
            studentName = studentName.toLowerCase();
        }
        if (StringUtils.isNotBlank(studentEmail)) {
            studentEmail = studentEmail.toLowerCase();
        }
        if (StringUtils.isNotBlank(studentMobileNo)) {
            studentMobileNo = studentMobileNo.toLowerCase();
        }
        if (StringUtils.isNotBlank(studentName) && StringUtils.isBlank(studentEmail) && StringUtils.isBlank(studentMobileNo)) {
            System.out.println("Search By StudentName");
            students = studentRepository.getAllByStudentName(studentName, pageable);
        } else if (StringUtils.isBlank(studentName) && StringUtils.isNotBlank(studentEmail) && StringUtils.isBlank(studentMobileNo)) {
            System.out.println("Search By StudentEmail");
            students = studentRepository.getAllByStudentEmail(studentEmail, pageable);
        } else if (StringUtils.isBlank(studentName) && StringUtils.isBlank(studentEmail) && StringUtils.isNotBlank(studentMobileNo)) {
            System.out.println("Search By StudentMobileNo");
            students = studentRepository.getAllByStudentMobileNo(studentMobileNo, pageable);
        } else if (StringUtils.isNotBlank(studentName) && StringUtils.isNotBlank(studentEmail) && StringUtils.isNotBlank(studentMobileNo)) {
            System.out.println("Search By StudentNameAndStudentEmail");
            students = studentRepository.getAllByStudentNameAndStudentEmail(studentName, studentEmail, pageable);
        } else if (StringUtils.isNotBlank(studentName) && StringUtils.isBlank(studentEmail) && StringUtils.isNotBlank(studentMobileNo)) {
            System.out.println("Search By StudentNameAndStudentMobileNo");
            students = studentRepository.getAllByStudentNameAndStudentMobileNo(studentName, studentMobileNo, pageable);
        } else if (StringUtils.isBlank(studentName) && StringUtils.isNotBlank(studentEmail) && StringUtils.isNotBlank(studentMobileNo)) {
            System.out.println("Search By StudentEmailAndStudentMobileNo");
            students = studentRepository.getAllByStudentEmailAndStudentMobileNo(studentEmail, studentMobileNo, pageable);
        } else if (StringUtils.isNotBlank(studentName) && StringUtils.isNotBlank(studentEmail) && StringUtils.isNotBlank(studentMobileNo)) {
            System.out.println("search By StudentNameAndStudentEmailAndStudentMobileNo");
            students = studentRepository.getAllByStudentNameAndStudentEmailAndStudentMobileNo(studentName, studentEmail, studentMobileNo, pageable);
        } else {
            students = studentRepository.getAllByDeleted(pageable);
        }
        return new pageDTO(students.getContent(), students.getTotalElements(), students.getNumber(), students.getTotalPages());
    }*/

}

