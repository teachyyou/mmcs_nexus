import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import LoginPage from './components/login/LoginPage';
import HomePage from './components/home/HomePage';
import './App.css';
import CompleteProfilePage from "./components/login/CompleteProfilePage";


function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/auth/status', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
                if (isAuthenticated) {
                    fetch('http://localhost:8080/api/v1/auth/verify_status', {
                        method: 'POST',
                        credentials: 'include'
                    })
                        .then(response => response.json()) // Теперь мы ожидаем JSON
                        .then(verified => {
                            console.log(verified)
                            setIsAuthenticated(verified); // Устанавливаем isAuthenticated в значение, возвращенное сервером
                        })
                        .catch(() => {
                            setIsAuthenticated(false); // Если произошла ошибка, сбрасываем аутентификацию
                        });
                }
                setIsLoading(false);
            })
    }, [isAuthenticated]);


    if (!isLoading) return (
        <Router>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={<HomePage isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />} />
                <Route path="/complete-profile" element={<CompleteProfilePage />} />
            </Routes>
        </Router>
    );
}

export default App;
