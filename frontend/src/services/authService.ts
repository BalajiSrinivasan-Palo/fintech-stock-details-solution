import axios from "axios";

const API_URL = process.env.REACT_APP_API_URL + "/api";

export interface LoginCredentials {
  username: string;
  password: string;
}

export const authService = {
  async login(credentials: LoginCredentials) {
    const response = await axios.post(`${API_URL}/auth/login`, credentials);
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("loginTime", Date.now().toString());
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("loginTime");
  },

  getToken() {
    const token = localStorage.getItem("token");
    const loginTime = localStorage.getItem("loginTime");

    if (token && loginTime) {
      const elapsed = Date.now() - parseInt(loginTime);
      if (elapsed > 5 * 60 * 1000) {
        // 5 minutes
        this.logout();
        return null;
      }
      return token;
    }
    return null;
  },
};
