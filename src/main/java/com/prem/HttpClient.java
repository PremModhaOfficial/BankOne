package com.prem;

import com.prem.server.config.ConfigurationManager;
import com.prem.server.config.HttpConfigurationException;
import com.prem.business.entities.User;
import com.prem.business.repositories.UserRepository;
import com.prem.clientInterface.AdminInterface;
import com.prem.clientInterface.UserInterface;
import com.prem.clientInterface.util.Repositories;
import com.prem.clientInterface.util.UserCred;
import com.prem.infrastructure.persistence.database.DatabaseAccountRepository;
import com.prem.infrastructure.persistence.database.DatabaseUserRepository;
import com.prem.infrastructure.persistence.inmemory.InMemoryAccountRepository;
import com.prem.infrastructure.persistence.inmemory.InMemoryUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

import javax.management.RuntimeErrorException;
import javax.management.relation.RelationTypeNotFoundException;

public class HttpClient {

    static final String COMFIG_PATH = "src/main/resources/http.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    static boolean inMemory;

    static {
        try {
            ConfigurationManager.getInstance().loadConfiguration(COMFIG_PATH);
            inMemory = ConfigurationManager
                    .getInstance()
                    .getCurrentConfiguration()
                    .getStorageConfig()
                    .getType()
                    .equals("in-memory");
        } catch (HttpConfigurationException e) {
            e.printStackTrace();
            inMemory = true;
        }
    }

    public static void main(String[] args) {
        String ans;

        boolean isAdmin = false;
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to BankOne");
        do {
            System.out.println("Login as Admin? [y/N]");
            ans = sc.nextLine().strip();
            if (ans.isEmpty() || (!ans.toLowerCase().startsWith("y") && !ans.toLowerCase().startsWith("n"))) {
                System.out.println("Please enter valid choice !!");
            }
        } while (ans.isEmpty() || (!ans.toLowerCase().startsWith("y") && !ans.toLowerCase().startsWith("n")));

        isAdmin = ans.toLowerCase().startsWith("n");

        launchInterface(isAdmin, sc);

    }

    private static void launchInterface(boolean isAdmin, Scanner sc) {
        UserCred userCred = UserCred.getInformation(sc);
        Repositories pair;
        if (inMemory) {
            pair = new Repositories(InMemoryUserRepository.getInstance(),
                    InMemoryAccountRepository.getInstance());
        } else {
            pair = new Repositories(
                    new DatabaseUserRepository(),
                    new DatabaseAccountRepository()

            );
        }

        if (isAdmin) {
            new AdminInterface(userCred, pair, sc);
        } else {
            new UserInterface(userCred, pair, sc);
        }

    }
}
