package server;

/**
 * Created by Николай on 25.03.2017.
 */
public class DBAuthService {

    public DBAuthService(){
        SQLHandler.connect();
    }

    public boolean setLogin(String name, String pass){
        return SQLHandler.getAuth(name, pass);
    }

    public void shutdown() {
        SQLHandler.disconnect();
        System.out.println("Базовый сервис авторизации завершил работу");
    }
}
