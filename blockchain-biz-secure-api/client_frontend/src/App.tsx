import React from 'react';
import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';
import LoginForm from './components/modules/auth/Login/LoginForm.tsx';
import ProtectedRoute from './components/Navigation/ProtectedRoute.tsx';
import Dashboard from "./components/Dashboard/Dashboard.tsx";
import {store} from "./redux/store.ts";
import {Provider} from "react-redux";

const App: React.FC = () => {
  return (
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginForm />} />
          <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                    <Provider store={store}>
                        <Dashboard />
                    </Provider>
                </ProtectedRoute>
              }
          />
        </Routes>
      </Router>
  );
};

export default App;
