import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import MainPageLayout from './layouts/MainPageLayout';
import AnalysisLayout from './layouts/AnalysisLayout';
import ControlLayout from './layouts/ControlLayout';

import DashboardPage from './modules/analysis/pages/DashboardPage';
import AnalysisPage from './modules/analysis/pages/AnalysisPage';
import PredictionPanel from './modules/analysis/pages/PredictionPage';

import ChatPage from "./modules/communication/pages/ChatPage";
import ChatWindow from "./modules/communication/pages/ChatWindow";
import LoginPage from "./modules/auth/pages/LoginPage";
import RegisterPage from "./modules/auth/pages/RegisterPage";

import ControlHomePage from "./modules/control/pages/ControlHomePage";
import DevicesListPage from "./modules/control/pages/DevicesListPage";
import DeviceCreatePage from "./modules/control/pages/DeviceCreatePage";
import DeviceActionsPage from "./modules/control/pages/DeviceActionsPage";
import DeviceParamsPage from "./modules/control/pages/DeviceParamsPage";
import DeviceDeletePage from "./modules/control/pages/DeviceDeletePage";
import DeviceAssignRoomPage from "./modules/control/pages/DeviceAssignRoomPage";

import RoomsListPage from "./modules/control/pages/RoomsListPage";
import RoomActionsPage from "./modules/control/pages/RoomActionsPage";
import RoomDevicesPage from "./modules/control/pages/RoomDevicesPage";
import RoomGroupControlPage from "./modules/control/pages/RoomGroupControlPage";

import PlansListPage from "./modules/control/pages/PlansListPage";
import PlanCreatePage from "./modules/control/pages/PlanCreatePage";
import PlanActionsPage from "./modules/control/pages/PlanActionsPage";
import PlanActivatePage from "./modules/control/pages/PlanActivatePage";
import PlanAddRulePage from "./modules/control/pages/PlanAddRulePage";
import PlanDeletePage from "./modules/control/pages/PlanDeletePage";

const ProtectedRoute = ({ children }) => {
    const token = sessionStorage.getItem("token");

    if (!token) {
        return <Navigate to="/login" replace/>;
    }

    return children;
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* logowanie i rejestracja */}
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/register" element={<RegisterPage/>}/>

                {/* strona główna */}
                <Route path="/" element={
                    <ProtectedRoute>
                        <MainPageLayout/>
                    </ProtectedRoute>
                }>
                    {/* analiza */}
                    <Route path="analiza" element={<AnalysisLayout/>}>
                        <Route index element={<DashboardPage/>}/>
                        <Route path="panel" element={<AnalysisPage/>}/>
                        <Route path="predykcja" element={<PredictionPanel/>}/>
                    </Route>

                    {/* komunikacja */}
                    <Route path="komunikacja" element={<ChatPage/>}>
                        <Route path=":chatId" element={<ChatWindow/>}/>
                    </Route>

                    {/*  sterowanie */}
                    <Route path="sterowanie" element={<ControlLayout />}>
                      <Route index element={<ControlHomePage />} />
                      <Route path="urzadzenia" element={<DevicesListPage />} />
                      <Route path="urzadzenia/nowe" element={<DeviceCreatePage />} />
                      <Route path="urzadzenia/:deviceId" element={<DeviceActionsPage />} />
                      <Route path="urzadzenia/:deviceId/parametry" element={<DeviceParamsPage />} />
                      <Route path="urzadzenia/:deviceId/przypisz" element={<DeviceAssignRoomPage />} />
                      <Route path="urzadzenia/:deviceId/usun" element={<DeviceDeletePage />} />
                      <Route path="pokoje" element={<RoomsListPage />} />
                      <Route path="pokoje/:roomId" element={<RoomActionsPage />} />
                      <Route path="pokoje/:roomId/urzadzenia" element={<RoomDevicesPage />} />
                      <Route path="pokoje/:roomId/grupowe" element={<RoomGroupControlPage />} />

                      <Route path="plany" element={<PlansListPage />} />
                      <Route path="plany/nowy" element={<PlanCreatePage />} />
                      <Route path="plany/:planId" element={<PlanActionsPage />} />
                      <Route path="plany/:planId/aktywuj" element={<PlanActivatePage />} />
                      <Route path="plany/:planId/reguly/dodaj" element={<PlanAddRulePage />} />
                      <Route path="plany/:planId/usun" element={<PlanDeletePage />} />
                    </Route>

                    {/* niedokończone moduły */}
                    <Route path="symulacja" element={<Placeholder title="Moduł Symulacji"/>}/>
                    <Route path="optymalizacja" element={<Placeholder title="Moduł Optymalizacji"/>}/>
                    <Route path="alerty" element={<Placeholder title="Moduł Alertów"/>}/>

                    <Route path="*" element={<Navigate to="/" replace/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

const Placeholder = ({ title }) => (
    <div className="panel" style={{ textAlign: 'center', padding: '3rem' }}>
        <h2>{title}</h2>
    </div>
);

export default App;