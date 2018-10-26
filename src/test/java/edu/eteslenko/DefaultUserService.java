package edu.eteslenko;

public class DefaultUserService {

    private String suffix;
    private JdbcUserDao userDao;

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public JdbcUserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(JdbcUserDao userDao) {
        this.userDao = userDao;
    }
}
