import axios from "axios";

// Global fetch interceptor to append JWT token automatically
const originalFetch = window.fetch;
window.fetch = async function (url, options = {}) {
  let requestUrl = "";
  if (typeof url === "string") {
    requestUrl = url;
  } else if (url && url.url) {
    requestUrl = url.url;
  }

  if (requestUrl.includes("/api") || requestUrl.startsWith("http://localhost:8080/api")) {
    const userStr = localStorage.getItem("user");
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        if (user && user.token) {
          if (url && typeof url.headers === "object" && typeof url.headers.set === "function") {
            url.headers.set("Authorization", `Bearer ${user.token}`);
          } else {
            options.headers = {
              ...options.headers,
              "Authorization": `Bearer ${user.token}`
            };
          }
        }
      } catch (e) {
        console.error("Error setting JWT auth header:", e);
      }
    }
  }
  return originalFetch(url, options);
};

// Global axios interceptor to append JWT token automatically
axios.interceptors.request.use((config) => {
  const userStr = localStorage.getItem("user");
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user && user.token) {
        config.headers = config.headers || {};
        config.headers["Authorization"] = `Bearer ${user.token}`;
      }
    } catch (e) {
      console.error("Error setting JWT auth header on global Axios:", e);
    }
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Intercept axios.create to apply the same JWT header interceptor to all custom instances
const originalAxiosCreate = axios.create;
axios.create = function (config) {
  const instance = originalAxiosCreate.call(axios, config);
  instance.interceptors.request.use((instanceConfig) => {
    const userStr = localStorage.getItem("user");
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        if (user && user.token) {
          instanceConfig.headers = instanceConfig.headers || {};
          instanceConfig.headers["Authorization"] = `Bearer ${user.token}`;
        }
      } catch (e) {
        console.error("Error setting JWT auth header on custom Axios instance:", e);
      }
    }
    return instanceConfig;
  }, (error) => {
    return Promise.reject(error);
  });
  return instance;
};
