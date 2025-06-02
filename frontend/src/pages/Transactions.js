import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TextField,
  InputAdornment,
  Chip,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  CircularProgress,
  Alert,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import transactionService from '../services/transactionService';
import studentService from '../services/studentService';
import bookService from '../services/bookService';

const Transactions = () => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [transactionType, setTransactionType] = useState('');
  const [status, setStatus] = useState('');
  const [openIssueDialog, setOpenIssueDialog] = useState(false);
  const [openReturnDialog, setOpenReturnDialog] = useState(false);
  
  // New state variables for API data
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [students, setStudents] = useState([]);
  const [books, setBooks] = useState([]);
  const [selectedStudent, setSelectedStudent] = useState('');
  const [selectedBook, setSelectedBook] = useState('');
  const [issueSuccess, setIssueSuccess] = useState(null);
  const [returnSuccess, setReturnSuccess] = useState(null);
  const [loadingStudentsBooks, setLoadingStudentsBooks] = useState(false);

  // Fetch transactions when component mounts
  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        setLoading(true);
        const data = await transactionService.getAllTransactions();
        setTransactions(data);
        setLoading(false);
      } catch (err) {
        console.error("Error fetching transactions:", err);
        setError("Failed to load transactions. Please try again later.");
        setLoading(false);
      }
    };

    fetchTransactions();
  }, []);

  // Fetch students and books for the dialogs
  useEffect(() => {
    const fetchStudentsAndBooks = async () => {
      try {
        setLoadingStudentsBooks(true);
        // Fetch real students from the API
        const studentsResponse = await studentService.getAllStudents();
        setStudents(studentsResponse || []);
        
        if (openIssueDialog) {
          // For issue dialog, fetch only available books
          const booksResponse = await bookService.getAllBooks();
          const availableBooks = booksResponse ? booksResponse.filter(book => book.available) : [];
          setBooks(availableBooks);
        } else if (openReturnDialog) {
          // For return dialog, fetch borrowed books
          const booksResponse = await bookService.getAllBooks();
          const borrowedBooks = booksResponse ? booksResponse.filter(book => !book.available) : [];
          setBooks(borrowedBooks);
        }
        
        setLoadingStudentsBooks(false);
      } catch (err) {
        console.error("Error fetching students and books:", err);
        setLoadingStudentsBooks(false);
      }
    };

    if (openIssueDialog || openReturnDialog) {
      fetchStudentsAndBooks();
    }
  }, [openIssueDialog, openReturnDialog]);

  // Handle issue book
  const handleIssueBook = async () => {
    if (!selectedStudent || !selectedBook) {
      return;
    }
    
    try {
      const response = await transactionService.issueBook(selectedBook, selectedStudent);
      setIssueSuccess("Book issued successfully!");
      setTimeout(() => {
        setIssueSuccess(null);
        setOpenIssueDialog(false);
        setSelectedStudent('');
        setSelectedBook('');
        // Refresh transactions
        transactionService.getAllTransactions().then(data => setTransactions(data));
      }, 2000);
    } catch (err) {
      console.error("Error issuing book:", err);
      setIssueSuccess("Failed to issue book. " + (err.response?.data?.error || "Please try again."));
    }
  };

  // Handle return book
  const handleReturnBook = async () => {
    if (!selectedStudent || !selectedBook) {
      return;
    }
    
    try {
      const response = await transactionService.returnBook(selectedBook, selectedStudent);
      setReturnSuccess("Book returned successfully!");
      setTimeout(() => {
        setReturnSuccess(null);
        setOpenReturnDialog(false);
        setSelectedStudent('');
        setSelectedBook('');
        // Refresh transactions
        transactionService.getAllTransactions().then(data => setTransactions(data));
      }, 2000);
    } catch (err) {
      console.error("Error returning book:", err);
      setReturnSuccess("Failed to return book. " + (err.response?.data?.error || "Please try again."));
    }
  };

  // Handle student selection in return dialog
  const handleStudentChangeForReturn = async (studentId) => {
    setSelectedStudent(studentId);
    setSelectedBook('');
    
    if (studentId) {
      try {
        setLoadingStudentsBooks(true);
        // Fetch books borrowed by this specific student
        const borrowedBooks = await bookService.getBorrowedBooksByStudent(studentId);
        setBooks(borrowedBooks || []);
        setLoadingStudentsBooks(false);
      } catch (err) {
        console.error("Error fetching borrowed books for student:", err);
        setLoadingStudentsBooks(false);
      }
    }
  };
  
  // Filter transactions based on search term, transaction type, and status
  const filteredTransactions = transactions.filter((transaction) => {
    const studentName = transaction.student ? transaction.student.name : '';
    const bookTitle = transaction.book ? transaction.book.name : '';
    
    const matchesSearch = 
      studentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      bookTitle.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (transaction.transactionId && transaction.transactionId.toLowerCase().includes(searchTerm.toLowerCase()));
      
    const matchesType = transactionType === '' || 
      (transaction.isIssueOperation && transactionType === 'ISSUE') || 
      (!transaction.isIssueOperation && transactionType === 'RETURN');
      
    const matchesStatus = status === '' || 
      (transaction.transactionStatus && transaction.transactionStatus === status);
    
    return matchesSearch && matchesType && matchesStatus;
  });

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleOpenIssueDialog = () => {
    setOpenIssueDialog(true);
  };

  const handleCloseIssueDialog = () => {
    setIssueSuccess(null);
    setOpenIssueDialog(false);
    setSelectedStudent('');
    setSelectedBook('');
  };

  const handleOpenReturnDialog = () => {
    setOpenReturnDialog(true);
  };

  const handleCloseReturnDialog = () => {
    setReturnSuccess(null);
    setOpenReturnDialog(false);
    setSelectedStudent('');
    setSelectedBook('');
  };

  return (
    <Box className="page-container">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" className="section-title">
          Transactions
        </Typography>
        <Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenIssueDialog}
            sx={{ mr: 2 }}
          >
            Issue Book
          </Button>
          <Button
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={handleOpenReturnDialog}
          >
            Return Book
          </Button>
        </Box>
      </Box>

      {/* Error message */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Search and Filters */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Search by student, book or transaction ID"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <FormControl fullWidth variant="outlined">
            <InputLabel id="type-label">Transaction Type</InputLabel>
            <Select
              labelId="type-label"
              id="type-select"
              value={transactionType}
              onChange={(e) => setTransactionType(e.target.value)}
              label="Transaction Type"
            >
              <MenuItem value="">
                <em>All Types</em>
              </MenuItem>
              <MenuItem value="ISSUE">Issue</MenuItem>
              <MenuItem value="RETURN">Return</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <FormControl fullWidth variant="outlined">
            <InputLabel id="status-label">Status</InputLabel>
            <Select
              labelId="status-label"
              id="status-select"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              label="Status"
            >
              <MenuItem value="">
                <em>All Statuses</em>
              </MenuItem>
              <MenuItem value="SUCCESSFUL">Successful</MenuItem>
              <MenuItem value="PENDING">Pending</MenuItem>
              <MenuItem value="FAILED">Failed</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      {/* Transactions Table */}
      <TableContainer component={Paper} sx={{ mb: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <Table sx={{ minWidth: 650 }} aria-label="transactions table">
            <TableHead>
              <TableRow sx={{ backgroundColor: 'primary.light' }}>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>ID</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Transaction UUID</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Student</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Book</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Type</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Status</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Date</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: 'white' }}>Fine</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredTransactions.length > 0 ? (
                filteredTransactions
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((transaction, index) => (
                    <TableRow
                      key={transaction.id || index}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    >
                      <TableCell>{transaction.id || `TXN-${index + 1}`}</TableCell>
                      <TableCell>{transaction.transactionId || 'N/A'}</TableCell>
                      <TableCell>{transaction.student ? transaction.student.name : 'N/A'}</TableCell>
                      <TableCell>{transaction.book ? transaction.book.name : 'N/A'}</TableCell>
                      <TableCell>
                        <Chip
                          label={transaction.isIssueOperation ? 'ISSUE' : 'RETURN'}
                          size="small"
                          color={transaction.isIssueOperation ? 'primary' : 'secondary'}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={transaction.transactionStatus || 'N/A'}
                          size="small"
                          color={
                            transaction.transactionStatus === 'SUCCESSFUL'
                              ? 'success'
                              : transaction.transactionStatus === 'PENDING'
                              ? 'warning'
                              : 'error'
                          }
                        />
                      </TableCell>
                      <TableCell>
                        {transaction.transactionDate 
                          ? new Date(transaction.transactionDate).toLocaleDateString() 
                          : 'N/A'}
                      </TableCell>
                      <TableCell>
                        {transaction.fineAmount !== null && transaction.fineAmount !== undefined 
                          ? `$${transaction.fineAmount.toFixed(2)}` 
                          : '-'}
                      </TableCell>
                    </TableRow>
                  ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    No transactions found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </TableContainer>

      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={filteredTransactions.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />

      {/* Issue Book Dialog */}
      <Dialog open={openIssueDialog} onClose={handleCloseIssueDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          Issue Book
          <IconButton
            aria-label="close"
            onClick={handleCloseIssueDialog}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          {issueSuccess && (
            <Alert 
              severity={issueSuccess.includes("Failed") ? "error" : "success"} 
              sx={{ mb: 2 }}
            >
              {issueSuccess}
            </Alert>
          )}
          {loadingStudentsBooks ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <FormControl fullWidth variant="outlined">
                  <InputLabel id="student-label">Student</InputLabel>
                  <Select
                    labelId="student-label"
                    label="Student"
                    value={selectedStudent}
                    onChange={(e) => setSelectedStudent(e.target.value)}
                  >
                    <MenuItem value="">
                      <em>Select Student</em>
                    </MenuItem>
                    {students.map((student) => (
                      <MenuItem key={student.id} value={student.id}>
                        {student.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth variant="outlined">
                  <InputLabel id="book-label">Book</InputLabel>
                  <Select
                    labelId="book-label"
                    label="Book"
                    value={selectedBook}
                    onChange={(e) => setSelectedBook(e.target.value)}
                  >
                    <MenuItem value="">
                      <em>Select Book</em>
                    </MenuItem>
                    {books.map((book) => (
                      <MenuItem key={book.id} value={book.id}>
                        {book.name || book.title || 'Unnamed Book'}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseIssueDialog}>Cancel</Button>
          <Button 
            variant="contained" 
            onClick={handleIssueBook}
            disabled={!selectedStudent || !selectedBook || loadingStudentsBooks}
          >
            Issue Book
          </Button>
        </DialogActions>
      </Dialog>

      {/* Return Book Dialog */}
      <Dialog open={openReturnDialog} onClose={handleCloseReturnDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          Return Book
          <IconButton
            aria-label="close"
            onClick={handleCloseReturnDialog}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          {returnSuccess && (
            <Alert 
              severity={returnSuccess.includes("Failed") ? "error" : "success"} 
              sx={{ mb: 2 }}
            >
              {returnSuccess}
            </Alert>
          )}
          {loadingStudentsBooks ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <FormControl fullWidth variant="outlined">
                  <InputLabel id="student-return-label">Student</InputLabel>
                  <Select
                    labelId="student-return-label"
                    label="Student"
                    value={selectedStudent}
                    onChange={(e) => handleStudentChangeForReturn(e.target.value)}
                  >
                    <MenuItem value="">
                      <em>Select Student</em>
                    </MenuItem>
                    {students.map((student) => (
                      <MenuItem key={student.id} value={student.id}>
                        {student.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth variant="outlined">
                  <InputLabel id="book-return-label">Book</InputLabel>
                  <Select
                    labelId="book-return-label"
                    label="Book"
                    value={selectedBook}
                    onChange={(e) => setSelectedBook(e.target.value)}
                  >
                    <MenuItem value="">
                      <em>Select Book</em>
                    </MenuItem>
                    {books.map((book) => (
                      <MenuItem key={book.id} value={book.id}>
                        {book.name || book.title || 'Unnamed Book'}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseReturnDialog}>Cancel</Button>
          <Button 
            variant="contained" 
            onClick={handleReturnBook}
            disabled={!selectedStudent || !selectedBook || loadingStudentsBooks}
          >
            Return Book
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Transactions;
