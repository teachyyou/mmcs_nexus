import React from 'react';
import LoginButton from './components/login/LoginButton';
import './App.css';

function App() {
    const handleLogin = () => {
        window.location.href = 'http://localhost:8080/login';
    };

    return (
        <div className="App">
            <header className="App-header">
                <LoginButton handleLogin={handleLogin} />
            </header>
        </div>
    );
}

export default App;
