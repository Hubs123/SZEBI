import axios from 'axios';

const API_URL = 'http://localhost:8080/api/alerts';

class AlertService {
    // Pobierz wszystkie alerty dla danej roli (np. RESIDENT)
    getAllAlerts(role = 'RESIDENT') {
        return axios.get(API_URL, {
            params: {
                role: role // Backend wymaga tego parametru!
            }
        });
    }

    // Pobierz pojedynczy alert
    getAlertById(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    // Usuń metody createAlert, updateAlert, deleteAlert jeśli użytkownik ma tylko czytać.
}

export default new AlertService();