package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {
    @Autowired
    private CartController cartController;
    @Autowired
    private ItemController itemController;
    @Autowired
    private OrderController orderController;
    @Autowired
    private UserController userController;
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CartRepository cartRepository = mock(CartRepository.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

    @Test
    public void contextLoads() {
    }

    //Cart controller test
    @Test
    @Transactional
    public void testAddToCart() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("addToCartTest");
        request.setPassword("addToCartTestPassword1");
        request.setConfirmPassword("addToCartTestPassword1");

        User user = userController.createUser(request).getBody();
        Assert.assertNotNull(user);

        ModifyCartRequest req2 = new ModifyCartRequest();
        req2.setUsername("addToCartTest");
        req2.setItemId(1);
        req2.setQuantity(1);

        Cart cart = cartController.addTocart(req2).getBody();
        Assert.assertEquals(1, cart.getItems().size());

        ModifyCartRequest req3 = new ModifyCartRequest();
        req3.setUsername("Random name");
        req3.setQuantity(2);
        req3.setItemId(1);

        Assert.assertEquals(404, cartController.addTocart(req3).getStatusCodeValue());

    }

    @Test
    @Transactional
    public void testRemoveFromCart() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("removeFromCartTest");
        req.setPassword("removeFromCartTestPassword1");
        req.setConfirmPassword("removeFromCartTestPassword1");

        User user = userController.createUser(req).getBody();
        Assert.assertNotNull(user);

        ModifyCartRequest req2 = new ModifyCartRequest();
        req2.setUsername("removeFromCartTest");
        req2.setItemId(1);
        req2.setQuantity(2);

        Cart cart = cartController.addTocart(req2).getBody();
        Assert.assertEquals(2, cart.getItems().size());

        ModifyCartRequest req3 = new ModifyCartRequest();
        req3.setUsername("removeFromCartTest");
        req3.setItemId(1);
        req3.setQuantity(1);
        Cart cart2 = cartController.removeFromcart(req3).getBody();

        Assert.assertEquals(1, cart2.getItems().size());

        ModifyCartRequest req4 = new ModifyCartRequest();
        req4.setUsername("null");
        req4.setItemId(1);
        req4.setQuantity(1);

        ResponseEntity<Cart> unknown = cartController.removeFromcart(req4);
        Assert.assertEquals(404, unknown.getStatusCodeValue());

    }

    //Item controller test
    @Test
    public void testGetItems() {
        ResponseEntity<List<Item>> itemsList = itemController.getItems();
        Assert.assertNotNull(itemsList);
        Assert.assertEquals(200, itemsList.getStatusCodeValue());
    }

    @Test
    public void testGetItemById() {
        Item item = new Item();
        item.setName("item");
        item.setDescription("desc");
        item.setPrice(BigDecimal.valueOf(10000));

        itemRepository.save(item);

        Item itemTest = itemController.getItemById(1L).getBody();
        Assert.assertEquals(1L, (long) itemTest.getId());

        ResponseEntity<Item> unknown = itemController.getItemById(10L);
        Assert.assertEquals(404, unknown.getStatusCodeValue());
    }

    @Test
    public void testGetItemsByName() {
        itemController = new ItemController();

        TestUtils.injectObjects(itemController, "itemRepository", itemRepository);
        Item item = new Item();
        List<Item> items = new ArrayList<>();
        item.setName("testItem");
        items.add(item);
        when(itemRepository.findByName("testItem")).thenReturn(items);
        List<Item> itemsList = itemController.getItemsByName("testItem").getBody();
        List<Item> itemsListExpected = itemRepository.findByName("testItem");

        Assert.assertEquals(itemsList.get(0), itemsListExpected.get(0));

        ResponseEntity<List<Item>> nullItems = itemController.getItemsByName("null");
        Assert.assertEquals(404, nullItems.getStatusCodeValue());

    }

    //Order controller test
    @Test
    public void testSubmitOrder() {
        orderController = new OrderController();

        TestUtils.injectObjects(orderController, "userRepository", userRepository);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepository);

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("desc");
        item.setPrice(BigDecimal.valueOf(50000));

        Cart cart = new Cart();
        cart.setId(1L);
        cart.addItem(item);
        cart.addItem(item);
        cart.setTotal(BigDecimal.valueOf(100000000));


        User user = new User();
        user.setUsername("testUser");
        user.setPassword("123456");
        user.setCart(cart);
        cart.setUser(user);

        when(userRepository.findByUsername("testUser")).thenReturn(user);

        ResponseEntity<UserOrder> order = orderController.submit("testUser");

        Assert.assertNotNull(order);
        Assert.assertEquals(200, order.getStatusCodeValue());
        Assert.assertEquals("testUser", order.getBody().getUser().getUsername());

        ResponseEntity<UserOrder> invalidOrder = orderController.submit("null");
        Assert.assertEquals(404, invalidOrder.getStatusCodeValue());
    }

    @Test
    public void testGetOrdersForUser() {
        orderController = new OrderController();

        TestUtils.injectObjects(orderController, "userRepository", userRepository);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepository);

        UserOrder order = orderController.submit("test").getBody();
        when(orderRepository.findByUser(userRepository.findByUsername("test"))).thenReturn(Collections.singletonList(order));
        ResponseEntity<List<UserOrder>> orders = orderController.getOrdersForUser("test");

        Assert.assertNotNull(orders);

        ResponseEntity<UserOrder> nullOrder = orderController.submit("null");
        Assert.assertEquals(404, nullOrder.getStatusCodeValue());

    }

    @Before
    public void addUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");

        userRepository.save(user);
    }

    //User controller test
    @Test
    public void testFindUserById() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepository);
        TestUtils.injectObjects(userController, "cartRepository", cartRepository);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);


        User user = new User();
        user.setUsername("test");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User user1 = userController.findById(1L).getBody();
        Assert.assertEquals(user.getUsername(), user1.getUsername());

        ResponseEntity<User> unknownUser = userController.findById(2L);
        Assert.assertEquals(404, unknownUser.getStatusCodeValue());
    }

    @Test
    public void testFindUserByUserName() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepository);
        TestUtils.injectObjects(userController, "cartRepository", cartRepository);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);

        User user = new User();
        user.setUsername("testUser");
        user.setPassword("123456");

        when(userRepository.findByUsername("testUser")).thenReturn(user);
        User userExpected = userController.findByUserName("testUser").getBody();

        Assert.assertNotNull(userExpected);

        User nullUser = userController.findByUserName("abcxyz").getBody();
        Assert.assertNull(nullUser);
    }

    @Test
    public void testCreateUser() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepository);
        TestUtils.injectObjects(userController, "cartRepository", cartRepository);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testUser1");
        request.setPassword("testUser1Password");
        request.setConfirmPassword("testUser1Password");
        ResponseEntity<User> response = userController.createUser(request);

        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusCodeValue());
        //Hashed password => Should not be the same
        Assert.assertNotEquals("testUser1Password", response.getBody().getPassword());

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setUsername("testUser2");
        request2.setPassword("asd");
        request2.setConfirmPassword("asd");

        ResponseEntity<User> response2 = userController.createUser(request2);
        //Short password => Fail
        Assert.assertEquals(400, response2.getStatusCodeValue());
    }
}
