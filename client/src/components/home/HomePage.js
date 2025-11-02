import React, {useEffect, useState} from 'react';
import { useAuth } from '../../AuthContext';

const HomePage = () => {
    return (
        <div className="App">
            {/* контент домашней без шапки — шапка рендерится глобально в AppContent */}
        </div>
    );
};

export default HomePage;
