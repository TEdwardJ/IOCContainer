package edu.eteslenko;

public class DefaultUserService {

    private String suffix;
    private JdbcUserDao jdbcUserDao;

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public JdbcUserDao getUserDao() {
        return jdbcUserDao;
    }

    public void setUserDao(JdbcUserDao userDao) {
        this.jdbcUserDao = userDao;
    }
}
