/**
 * 
 */
package no.hvl.dat152.rest.ws.service;

import java.util.List;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.repository.OrderRepository;

/**
 * @author tdoy
 */
@Service
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;
	
	public Order saveOrder(Order order) {
		
		order = orderRepository.save(order);
		
		return order;
	}
	
	public Order findOrder(Long id) throws OrderNotFoundException {
		
		Order order = orderRepository.findById(id)
				.orElseThrow(()-> new OrderNotFoundException("Order with id: "+id+" not found in the order list!"));
		
		return order;
	}
	
	// TODO public void deleteOrder(Long id)
	public void deleteOrder(Long id) throws OrderNotFoundException {

		Order order = findOrder(id);

		orderRepository.delete(order);
	}

	// TODO public List<Order> findAllOrders()
	public List<Order> findAllOrders(Pageable page) {

		return orderRepository.findAll(page).getContent();
	}

	// TODO public List<Order> findByExpiryDate(LocalDate expiry, Pageable page)
	public List<Order> findByExpiryDate(LocalDate expiry, Pageable page) {

		// "expires by" is inclusive, findByExpiryBefore is exclusive
		return orderRepository.findByExpiryBefore(expiry.plusDays(1), page).getContent();
	}

	// TODO public Order updateOrder(Order order, Long id)
	public Order updateOrder(Order order, Long id) throws OrderNotFoundException {

		Order existing = findOrder(id);
		existing.setIsbn(order.getIsbn());
		existing.setExpiry(order.getExpiry());

		return orderRepository.save(existing);
	}

}
