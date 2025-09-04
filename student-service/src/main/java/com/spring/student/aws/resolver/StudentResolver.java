package com.spring.student.aws.resolver;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.spring.student.aws.model.Student;
import com.spring.student.aws.repository.StudentRepository;

import jakarta.annotation.PostConstruct;

/**
 * Entity Resolver (DGS Federated type)
 */
@DgsComponent
public class StudentResolver {

	@PostConstruct
	public void logSchemaLoaded() {
		System.out.println("✅ DGS schema loaded successfully!");
	}

	private final StudentRepository studentRepository;

	public StudentResolver(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	@DgsMutation
	public Student addStudent(@InputArgument String name, @InputArgument String email) {
		Student newStudent = new Student(UUID.randomUUID().toString(), name, email);
		return studentRepository.save(newStudent);
	}

	@DgsMutation
	public Student updateStudent(@InputArgument String id, @InputArgument String name, @InputArgument String email) {
		Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
		student.setName(name);
		student.setEmail(email);
		return studentRepository.save(student);
	}

	@DgsMutation
	public Boolean deleteStudent(@InputArgument String id) {
		studentRepository.deleteById(id);
		return true;
	}

	@DgsQuery
	public List<Student> allStudents() {
		System.out.println("<<<<<<<<<<< allStudents >>>>>>>>>>");
		return studentRepository.findAll();
	}

	@DgsQuery
	public Student studentById(@InputArgument String id) {
		return studentRepository.findById(id).orElse(null);
	}
	
}
