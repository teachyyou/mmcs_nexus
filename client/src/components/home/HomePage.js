import React, {useState, useEffect} from 'react';
import { useNavigate } from 'react-router-dom';
import LoginButton from '../login/LoginButton';

const HomePage = () => {
    const navigate = useNavigate();
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        // Check if the user is authenticated
        fetch('http://localhost:8080/api/auth/status', {
            credentials: 'include'
        })
            .then(response => {
                return response.json();
            })
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
            })
            .catch(error => {
                console.error('There was a problem with the fetch operation:', error);
            });
    }, []);

    const handleLogin = () => {
        navigate('/login');
    };

    const handleLogout = () => {
        fetch('http://localhost:8080/logout', {
            method: 'POST',
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                setIsAuthenticated(false);
                navigate('/');
            })
            .catch(error => {
                console.error('There was a problem with the fetch operation:', error);
            });
    };

    return (
        <div className="App">
            <header className="App-header">
                {isAuthenticated ? (
                    <button onClick={handleLogout}>Logout</button>
                ) : (
                    <LoginButton handleLogin={handleLogin} />
                )}
            </header>
        </div>
    );
};

export default HomePage;
