package com.Mahdi.Store.services;

import com.Mahdi.Store.Models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product,Integer> {  //in the jpa, should have the model name and the type of the primary key


}
