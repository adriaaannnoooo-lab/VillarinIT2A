package ui;

import dao.ActivityLogDao;
import dao.ConsignmentDao;
import dao.ProductDao;
import dao.ResellerDao;
import dao.UserDao;

import java.util.Scanner;

/**
 * Holds the single shared Scanner and DAO objects created in Main,
 * so every menu uses the same instances instead of creating its own.
 */
public record AppContext(Scanner scanner, Input input, UserDao userDao, ActivityLogDao logDao,
                         ProductDao productDao, ResellerDao resellerDao,
                         ConsignmentDao consignmentDao) {
}
