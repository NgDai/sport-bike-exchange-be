package com.bicycle.marketplace.service;

import com.bicycle.marketplace.Repository.Impl.IUserRepository;
import com.bicycle.marketplace.Repository.UserRepository;
import com.bicycle.marketplace.entity.User;
import com.bicycle.marketplace.service.Impl.IUserService;

import java.util.ArrayList;
import java.util.List;

public class UserService implements IUserService{

    private IUserRepository userRepository;

    public UserService(){
        this.userRepository = new UserRepository();
    }

    public UserService(IUserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public void create(User user) {
        try{
            userRepository.createUser(user);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {
        try{
            userRepository.updateUser(user);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int userId) {
        try{
            userRepository.deleteUser(userId);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public User findById(int userId) {
        try{
           return userRepository.findUserById(userId);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll() {
        try{
            return userRepository.listAllUsers();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
