package com.StudentLibrary.Studentlibrary.Repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Model.TransactionStatus;

@Transactional
public interface TransactionRepository extends JpaRepository<Transaction,Integer> {

    @Query("select t from Transaction t where t.student.id=:student_id and t.book.id=:book_id and t.transactionStatus=:status and t.isIssueOperation=:isIssue")
    public List<Transaction> findByStudent_Book(@Param("student_id") int student_id,
                                           @Param("book_id") int book_id,
                                           @Param("status") TransactionStatus status,
                                           @Param("isIssue") boolean isIssue);
}
