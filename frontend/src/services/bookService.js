import api from './api';

const bookService = {
  getAllBooks: async () => {
    try {
      console.log("bookService: Calling /api/books/public/all endpoint");
      const response = await api.get('/api/books/public/all');
      console.log("bookService: Response received:", response.status);
      console.log("bookService: Response data type:", typeof response.data);
      console.log("bookService: Response data is array:", Array.isArray(response.data));
      if (Array.isArray(response.data)) {
        console.log("bookService: Number of books:", response.data.length);
      } else {
        console.log("bookService: Response data:", response.data);
      }
      return response.data;
    } catch (error) {
      console.error("bookService: Error in getAllBooks:", error);
      throw error;
    }
  },

  getBookById: async (id) => {
    try {
      const response = await api.get(`/api/books/public/findById?id=${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  createBook: async (bookData) => {
    try {
      const response = await api.post('/api/books/create', bookData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
  
  createBookWithImage: async (formData) => {
    try {
      console.log("Sending form data to server:");
      
      // Log form data contents for debugging
      for (let pair of formData.entries()) {
        console.log(pair[0] + ': ' + (pair[0] === 'image' ? 'File object' : pair[1]));
      }
      
      // Explicitly set Content-Type to false to let the browser set it correctly with boundary
      const response = await api.post('/api/books/public/createWithImage', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      console.error("Error in createBookWithImage:", error);
      throw error;
    }
  },

  updateBook: async (bookData) => {
    try {
      console.log("bookService: Updating book with data:", JSON.stringify(bookData));
      const response = await api.put('/api/books/public/updateBook', bookData);
      console.log("bookService: Update response:", response.status, response.data);
      return response.data;
    } catch (error) {
      console.error("bookService: Error updating book:", error);
      throw error;
    }
  },

  deleteBook: async (id) => {
    try {
      const response = await api.delete(`/api/books/public/deleteBook?id=${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  searchBooks: async (params) => {
    try {
      const response = await api.get('/api/books/search', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  },
  
  getBorrowedBooksByStudent: async (studentId) => {
    try {
      console.log("bookService: Fetching borrowed books for student ID:", studentId);
      const response = await api.get(`/api/books/public/borrowed?studentId=${studentId}`);
      console.log("bookService: Borrowed books response:", response.data);
      return response.data;
    } catch (error) {
      console.error("bookService: Error fetching borrowed books:", error);
      throw error;
    }
  },
  
  getBookImageUrl: (id) => {
    return `${api.defaults.baseURL}/api/books/public/image?id=${id}&timestamp=${new Date().getTime()}`;
  }
};

export default bookService;
