package com.mbaas.controllers;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.persistence.DataQueryBuilder;
import com.mbaas.models.Meta;
import com.mbaas.models.Place;
import com.mbaas.models.Position;
import com.mbaas.services.Geo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/location")
@AllArgsConstructor
public class LocationController {
    @GetMapping
    public ModelAndView getPlaces(ModelAndView modelAndView) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        try {
            List<Place> places = Backendless.Data.of(Place.class).find(
                    DataQueryBuilder.create().setWhereClause(String.format("ownerId='%s'", user.getUserId()))
            );
            modelAndView.addObject("places", places);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        modelAndView.addObject("find", false);
        modelAndView.setViewName("location.html");
        return modelAndView;
    }

    @PostMapping
    public ModelAndView findPlaces(ModelAndView modelAndView, @RequestParam("location") String location) {
        List<Place> places = Backendless.Data.of(Place.class).find(
                DataQueryBuilder.create().setWhereClause(String.format("name LIKE '%%%1$s%%' OR description LIKE '%%%1$s%%'", location))
        );
        modelAndView.addObject("find", true);
        modelAndView.addObject("places", places);
        modelAndView.setViewName("location.html");
        return modelAndView;
    }

    @GetMapping("/{name}")
    public ModelAndView getLocation(ModelAndView modelAndView, @PathVariable("name") String name) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        try {
            Place place = Backendless.Data.of(Place.class).find(
                    DataQueryBuilder.create().setWhereClause(String.format("name='%s'", name))
            ).stream().findFirst().orElse(null);
            modelAndView.addObject("place", place);
            assert place != null;
            modelAndView.addObject("mapLink", Geo.generateGoogleMapsLink(place.getLatitude(), place.getLongitude()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        modelAndView.setViewName("loc.html");
        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView getCreatePlaceForm(ModelAndView modelAndView) {
        modelAndView.setViewName("create-place.html");
        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView createPlace(@RequestParam String name,
                                    @RequestParam String description,
                                    @RequestParam String picture,
                                    @RequestParam("meta") String metaStr,
                                    @RequestParam(defaultValue = "0") String latitude,
                                    @RequestParam(defaultValue = "0") String longitude,
                                    @RequestParam String coord) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        Position position = (coord.equals("Current")) ?
                Geo.getCurrentPosition() : new Position(Double.parseDouble(latitude), Double.parseDouble(longitude));
        Place place = new Place(name, description, picture, position.getLatitude(), position.getLongitude(), metaStr);
        Backendless.Data.of(Place.class).save(place);
        return new ModelAndView("redirect:/location");
    }

    @PostMapping("/remove/{name}")
    public ModelAndView removePlace(@PathVariable String name) {
        BackendlessUser user = Backendless.UserService.CurrentUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        Backendless.Data.of(Place.class).remove(String.format("name='%s'", name));
        return new ModelAndView("redirect:/location");
    }
}
