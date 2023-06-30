package com.mbaas.controllers;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.files.FileInfo;
import com.backendless.persistence.DataQueryBuilder;
import com.mbaas.models.FriendRequest;
import com.mbaas.models.Friends;
import com.mbaas.models.Statistic;
import com.mbaas.models.UserDTO;
import com.mbaas.services.BackendlessDiscService;
import com.mbaas.services.mappers.UserDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {
    private final UserDTOMapper userDTOMapper;

    @GetMapping("/account")
    public ModelAndView accountPage(ModelAndView modelAndView,
                                    @RequestParam(name = "error", defaultValue = "false") Boolean error) {
        if (Backendless.UserService.CurrentUser() == null) {
            return new ModelAndView("redirect:/login");
        }
        modelAndView.addObject("user", Backendless.UserService.CurrentUser());
        modelAndView.setViewName("account.html");
        return modelAndView;
    }

    @PostMapping("/account")
    public ModelAndView accountCompute(@RequestParam("age") Integer age,
                                       @RequestParam("country") String country,
                                       @RequestParam("updateGeo") String updateGeo) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        user.setProperty("age", age);
        user.setProperty("country", country);
        if (updateGeo.equals("yes")) {
            user.setProperty("isMarked", true);
        } else {
            user.setProperty("isMarked", false);
        }
        Backendless.UserService.update(user);
        return new ModelAndView("redirect:/account");
    }

    @GetMapping("/account/choose-image")
    public ModelAndView chooseAccountImagePage(ModelAndView modelAndView) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        List<FileInfo> files = Backendless.Files.listing(user.getProperty("name").toString())
                .stream()
                .filter(e -> !e.getName().equals("shared") && (e.getName().contains(".png") || e.getName().contains(".jpg")))
                .collect(Collectors.toList());
        modelAndView.addObject("files", files);
        modelAndView.setViewName("choose-image.html");
        return modelAndView;
    }

    @PostMapping("/account/choose-image/{name}")
    public ModelAndView setAccountImage(@PathVariable("name") String name) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        FileInfo file = Backendless.Files.listing(user.getProperty("name").toString())
                .stream()
                .filter(e -> e.getName().equals(name))
                .findFirst().orElse(null);
        assert file != null;
        user.setProperty("pictureUrl", file.getPublicUrl());
        Backendless.UserService.update(user);
        return new ModelAndView("redirect:/account");
    }

    @PostMapping("/account/exit")
    public ModelAndView accountLogout() {
        Backendless.UserService.logout();
        Statistic statistic = Backendless.Data.of(Statistic.class).find().stream().findFirst().orElse(null);
        assert statistic != null;
        statistic.setOnlineUsersCount(statistic.getOnlineUsersCount() - 1);
        Backendless.Data.of(Statistic.class).save(statistic);
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView loginPage(ModelAndView modelAndView,
                                  @RequestParam(name = "error", defaultValue = "false") Boolean error) {
        modelAndView.addObject("error", error);
        modelAndView.setViewName("login.html");
        return modelAndView;
    }

    @PostMapping("/login")
    public ModelAndView loginCompute(ModelAndView modelAndView,
                                     @RequestParam("email") String email,
                                     @RequestParam("password") String password) {
        try {
            Backendless.UserService.login(email, password, true);
            Statistic statistic = Backendless.Data.of(Statistic.class).find().stream().findFirst().orElse(null);
            assert statistic != null;
            statistic.setOnlineUsersCount(statistic.getOnlineUsersCount() + 1);
            Backendless.Data.of(Statistic.class).save(statistic);
        } catch (Exception ex) {
            Statistic statistic = new Statistic();
            Backendless.Data.of(Statistic.class).save(statistic);
            return loginPage(modelAndView, true);
        }
        return new ModelAndView("redirect:/account");
    }

    @GetMapping("/registration")
    public ModelAndView registrationPage(ModelAndView modelAndView,
                                         @RequestParam(name = "error", defaultValue = "false") Boolean error) {
        modelAndView.addObject("error", error);
        modelAndView.setViewName("registration.html");
        return modelAndView;
    }

    @PostMapping("/registration")
    public ModelAndView registrationCompute(ModelAndView modelAndView,
                                            @RequestParam("name") String name,
                                            @RequestParam("email") String email,
                                            @RequestParam("password") String password,
                                            @RequestParam("age") Integer age,
                                            @RequestParam("sex") String sex,
                                            @RequestParam("country") String country) {
        if (age <= 5) {
            return registrationPage(modelAndView, true);
        }
        try {
            BackendlessUser user = new BackendlessUser();
            user.setEmail(email);
            user.setPassword(password);
            user.setProperty("name", name);
            user.setProperty("age", age);
            user.setProperty("sex", sex);
            user.setProperty("country", country);
            Backendless.UserService.register(user);
            Backendless.UserService.login(user.getEmail(), user.getPassword());

            String directory = String.format("/%s/shared", name);
            BackendlessDiscService.createDirectory(directory);
            Statistic statistic = Backendless.Data.of(Statistic.class).find().stream().findFirst().orElse(null);
            assert statistic != null;
            statistic.setOnlineUsersCount(statistic.getOnlineUsersCount() + 1);
            Backendless.Data.of(Statistic.class).save(statistic);
        } catch (Exception ex) {
            Statistic statistic = new Statistic();
            Backendless.Data.of(Statistic.class).save(statistic);
            return registrationPage(modelAndView, true);
        }
        return new ModelAndView("redirect:/account");
    }

    @GetMapping("/remake-password")
    public ModelAndView remakePassword(ModelAndView modelAndView,
                                       @RequestParam(name = "error", defaultValue = "false") Boolean error) {
        modelAndView.setViewName("remake-password.html");
        return modelAndView;
    }

    @PostMapping("/remake-password")
    public ModelAndView remakePasswordCompute(@RequestParam(name = "email") String email) {
        try {
            Backendless.UserService.restorePassword(email);
        } catch (Exception ex) {
            return remakePassword(new ModelAndView(), true);
        }
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/friends")
    public ModelAndView getFriends(ModelAndView modelAndView) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        try {
            List<Friends> friends = Backendless.Data.of(Friends.class)
                    .find(
                            DataQueryBuilder.create()
                                    .setWhereClause(String.format("friend1='%1$s' OR friend2='%1$s'", user.getUserId()))
                    );
            List<UserDTO> users = friends
                    .stream()
                    .filter(f -> f.getFriend1().equals(user.getUserId()) || f.getFriend2().equals(user.getUserId()))
                    .map(f -> (f.getFriend1().equals(user.getUserId())) ? f.getFriend2() : f.getFriend1())
                    .map(id -> Backendless.Data.of(BackendlessUser.class).findById(id))
                    .map(userDTOMapper)
                    .collect(Collectors.toList());
            modelAndView.addObject("users", users);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        modelAndView.addObject("find", false);
        modelAndView.setViewName("friends.html");
        return modelAndView;
    }

    @PostMapping("/friends")
    public ModelAndView findUsers(ModelAndView modelAndView, @RequestParam("username") String username) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        List<BackendlessUser> backendlessUsers = Backendless.Data.of(BackendlessUser.class)
                .find(
                        DataQueryBuilder.create().setWhereClause(String.format("name LIKE '%%%s%%'", username))
                );
        List<UserDTO> users = backendlessUsers
                .stream()
                .filter(u -> !u.getUserId().equals(user.getUserId()))
                .map(userDTOMapper)
                .collect(Collectors.toList());
        modelAndView.addObject("users", users);
        modelAndView.addObject("find", true);
        modelAndView.setViewName("friends.html");
        return modelAndView;
    }

    @PostMapping("/friends/add/{id}")
    public ModelAndView addUserToFriend(@PathVariable("id") String id) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        Backendless.Data.of(FriendRequest.class).save(new FriendRequest(user.getUserId(), id));
        return new ModelAndView("redirect:/friends");
    }

    @PostMapping("/friends/remove/{id}")
    public ModelAndView removeUserFromFriend(@PathVariable("id") String id) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        Backendless.Data.of(Friends.class).remove(
                String.format("(friend1='%1$s' AND friend2='%2$s') OR (friend1='%2$s' AND friend2='%1$s')", user.getUserId(), id)
        );
        return new ModelAndView("redirect:/friends");
    }

    @GetMapping("/friends/requests")
    public ModelAndView getFriendRequests(ModelAndView modelAndView) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        try {
            List<FriendRequest> friends = Backendless.Data.of(FriendRequest.class)
                    .find(
                            DataQueryBuilder.create()
                                    .setWhereClause(String.format("receiver='%s'", user.getUserId()))
                    );
            List<UserDTO> users = friends
                    .stream()
                    .map(f -> Backendless.Data.of(BackendlessUser.class).findById(f.getOwner()))
                    .map(userDTOMapper)
                    .collect(Collectors.toList());
            modelAndView.addObject("users", users);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        modelAndView.setViewName("request-friends.html");
        return modelAndView;
    }

    @PostMapping("/friends/requests/confirm/{id}")
    public ModelAndView confirmFriendRequest(@PathVariable("id") String id) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        Backendless.Data.of(FriendRequest.class).remove(String.format("owner='%s' AND receiver='%s'", id, user.getUserId()));
        Backendless.Data.of(Friends.class).save(new Friends(user.getUserId(), id));
        return new ModelAndView("redirect:/friends/requests");
    }

    @PostMapping("/friends/requests/remove/{id}")
    public ModelAndView removeFriendRequest(@PathVariable("id") String id) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        Backendless.Data.of(FriendRequest.class).remove(String.format("owner='%s' AND receiver='%s'", id, user.getUserId()));
        return new ModelAndView("redirect:/friends/requests");
    }
}
