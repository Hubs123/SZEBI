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
import AdministrationPage from "./modules/control/pages/AdministrationPage";

import { requireAdmin, requireResident } from "./services/roleGuards";

import AlertsLayout from './layouts/AlertsLayout';
import AlertsPage from './modules/alerts/pages/AlertsPage';
import DeviceGroupsPage from './modules/alerts/pages/DeviceGroupsPage';

const ProtectedRoute = ({ children }) => {
    const token = sessionStorage.getItem("token");

    if (!token) {
        return <Navigate to="/login" replace/>;
    }

    return children;
}

const RoleProtectedRoute = ({ guard, children }) => {
    const token = sessionStorage.getItem("token");
    if (!token) {
        return <Navigate to="/login" replace/>;
    }
    if (!guard || !guard()) {
        return <Navigate to="/" replace/>;
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

                      {/* Administracja (ROLE_ADMIN) */}
                      <Route path="administracja" element={
                        <RoleProtectedRoute guard={requireAdmin}>
                          <AdministrationPage />
                        </RoleProtectedRoute>
                      } />

                      {/* Pozostałe sekcje sterowania (ROLE_USER) */}
                      <Route path="urzadzenia" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <DevicesListPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="urzadzenia/nowe" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <DeviceCreatePage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="urzadzenia/:deviceId" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <DeviceActionsPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="urzadzenia/:deviceId/parametry" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <DeviceParamsPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="urzadzenia/:deviceId/przypisz" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <DeviceAssignRoomPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="urzadzenia/:deviceId/usun" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <DeviceDeletePage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="pokoje" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <RoomsListPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="pokoje/:roomId" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <RoomActionsPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="pokoje/:roomId/urzadzenia" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <RoomDevicesPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="pokoje/:roomId/grupowe" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <RoomGroupControlPage />
                        </RoleProtectedRoute>
                      } />

                      <Route path="plany" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <PlansListPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="plany/nowy" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <PlanCreatePage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="plany/:planId" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <PlanActionsPage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="plany/:planId/aktywuj" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <PlanActivatePage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="plany/:planId/reguly/dodaj" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <PlanAddRulePage />
                        </RoleProtectedRoute>
                      } />
                      <Route path="plany/:planId/usun" element={
                        <RoleProtectedRoute guard={requireResident}>
                          <PlanDeletePage />
                        </RoleProtectedRoute>
                      } />
                    </Route>

                    {/* alerty */}
                    <Route path="alerty" element={<AlertsLayout />}>
                        <Route index element={<AlertsPage />} />
                        <Route path="konfiguracja" element={<DeviceGroupsPage />} />
                    </Route>

                    {/* niedokończone moduły */}
                    <Route path="symulacja" element={<Placeholder title="Moduł Symulacji"/>}/>
                    <Route path="optymalizacja" element={<Placeholder title="Moduł Optymalizacji"/>}/>
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