import React from 'react';
import { useNavigate } from 'react-router-dom';
import LoginButton from '../login/LoginButton';

const HomePage = () => {
    const navigate = useNavigate();

    const handleLogin = () => {
        navigate('/login');
    };

    return (
        <div className="App">
            <header className="App-header">
                <LoginButton handleLogin={handleLogin} />
            </header>
        </div>
    );
};

export default HomePage;
