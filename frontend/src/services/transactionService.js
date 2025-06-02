import api from './api';

const transactionService = {
  getAllTransactions: async () => {
    try {
      const response = await api.get('/transaction/all');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  getTransactionById: async (id) => {
    try {
      const response = await api.get(`/transaction/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  issueBook: async (bookId, cardId) => {
    try {
      const response = await api.post(`/transaction/issueBook?bookId=${bookId}&studentId=${cardId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  returnBook: async (bookId, cardId) => {
    try {
      const response = await api.post(`/transaction/returnBook?bookId=${bookId}&studentId=${cardId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  getStudentTransactions: async (cardId) => {
    try {
      const response = await api.get(`/transaction/student?cardId=${cardId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  getBookTransactions: async (bookId) => {
    try {
      const response = await api.get(`/transaction/book?bookId=${bookId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },
  
  getOverdueTransactions: async () => {
    try {
      const response = await api.get('/transaction/overdue');
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default transactionService;
