import java.util.ArrayList;
import java.util.HashMap;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;
import java.util.List;

public class App {
  public static void main(String[] args) {
    staticFileLocation("/public");
    String layout = "templates/layout.vtl";

    //ORDERS
    //TODO: update ALL order routes to decrement inventory as needed

    get("/servers/orders/active", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("orders", Order.getAllActive());
      model.put("template", "templates/orders-active.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    //Order - take a new order
    post("/orders/new", (request, response) -> {
      int table = Integer.parseInt(request.queryParams("table"));
      int seat = Integer.parseInt(request.queryParams("seat"));
      for (Dish dish : Dish.all()) {
        Integer dishQuantity = Integer.parseInt(request.queryParams(dish.getName()));
        if (dishQuantity > 0) {
          for (Integer i = dishQuantity; i > 0; i--) {
            Order order = new Order (table, seat, dish.getId());
            order.save();
          }
        }
      }
      response.redirect("/servers/orders/active");
      return null;
    });

    //Order - pay for an order
    post("/servers/orders/:id/pay", (request, response) -> {
      Order thisOrder = Order.find(Integer.parseInt(request.params("id")));
      thisOrder.pay();
      response.redirect("/servers/orders/active");
      return null;
    });

    //Order - complete an order and make it no longer active
    post("/servers/orders/:id/complete", (request, response) -> {
      Order thisOrder = Order.find(Integer.parseInt(request.params("id")));
      thisOrder.complete();
      response.redirect("/servers/orders/active");
      return null;
    });

    //Order - cancel and lost ingredients i.e diner walked out
    post("/servers/orders/active/remove", (request, response) -> {
      Order thisOrder = Order.find(
        Integer.parseInt(request.queryParams("order-remove")));
      thisOrder.complete();
      response.redirect("/servers/orders/active");
      return null;
    });

    get("/kitchen/orders/active", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("orders", Order.getAllActiveOrderByTime());
      model.put("dishes", Dish.all());
      model.put("template", "templates/orders-active.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    get("/servers/orders/new", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("orders", Order.getAllActive());
      model.put("dishes", Dish.all());
      model.put("template", "templates/orders-new.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    //Order - individual order page
    get("/servers/orders/:id", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("order", Order.find(Integer.parseInt(request.params("id"))));
      model.put("dishes", Dish.all());
      model.put("template", "templates/order.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    //Order - change dish i.e. diner changed mind
    post("/servers/orders/:id/modify", (request, response) -> {
      Order thisOrder = Order.find(Integer.parseInt(request.params("id")));
      Dish requestedDish = Dish.find(Integer.parseInt(request.queryParams("select-dish")));
      thisOrder.changeDish(requestedDish.getId());
      response.redirect("/servers/orders/" + thisOrder.getId());
      return null;
    });

    //Order - lost ingredients, cancel and restart i.e. diner sent it back
    post("/servers/orders/active/restart", (request, response) -> {
      Order thisOrder = Order.find(
        Integer.parseInt(request.queryParams("order-restart")));
      thisOrder.complete();
      Order newOrder = new Order(thisOrder.getTable(), thisOrder.getSeat(), thisOrder.getDishId());
      newOrder.save();
      response.redirect("/servers/orders/" + newOrder.getId());
      return null;
    });

    //INGREDIENTS
    get("/manager/ingredients/:id", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("ingredient", Ingredient.find(Integer.parseInt(request.params("id"))));
      model.put("template", "templates/ingredient.vtl");
      return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

    get("/manager/new-ingredient", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("template", "templates/ingredient-new.vtl");
      return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

    //INVENTORY
    get("/manager/inventory", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("ingredients", Ingredient.all());
      model.put("dishes", Dish.all());
      model.put("template", "templates/ingredients-inventory.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    get("/manager/delivery", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("ingredients", Ingredient.all());
      model.put("template", "templates/ingredients-delivery.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    //DISHES
    get("/manager/orders/dishes", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("dishes", Dish.all());
      model.put("template", "templates/dishes.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    post("/manager/dishes/:id", (request, response) -> {
      Dish dish = new Dish(request.queryParams("dish-name"));
      dish.save();
      response.redirect("/manager/orders/dishes");
      return null;
    });

    get("/manager/dishes/:id", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("dish", Dish.find(Integer.parseInt(request.params(":id"))));
      model.put("recipes", Recipe.all());
      model.put("template", "templates/dish.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());
  }
}
