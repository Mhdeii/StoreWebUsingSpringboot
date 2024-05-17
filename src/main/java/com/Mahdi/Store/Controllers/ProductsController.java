package com.Mahdi.Store.Controllers;

import com.Mahdi.Store.Models.Product;
import com.Mahdi.Store.Models.ProductDto;
import com.Mahdi.Store.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")   //access at the url that starts with products url.
public class ProductsController {
    //request repo from service container

    @Autowired
    private ProductsRepository repo;

    //method that allows us to read the products from the database

    @GetMapping({"", "/"}) //accessible at url /products or /products/

    public String showProductList (Model model){
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC,"id")); //findall() method that retrive products from db and parameter inside of it for sorting
        model.addAttribute("products", products);
        return "products/index";
    }


    //method that displays the form for the product to be filled from the user

    @GetMapping("/create")
    public String showCreatePage(Model model){  //model allows us to add data accessible to the page
        ProductDto productDto = new ProductDto();  //product dto object
        model.addAttribute("productDto", productDto);  // the obj to model to bind it to the form
        return "products/CreateProduct";
    }

    //method that creates products

    @PostMapping("/create")  //post method
    public String createProduct(
        @Valid @ModelAttribute ProductDto productDto,  //object, and validate data for it using @valid
                BindingResult result  //check the data if its error or something
                ) {

        //validate the image
        if (productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto", "imageFile", "The image file is required!"));
        }

        if (result.hasErrors()){
            return "products/CreateProduct";
        }

        //saving image file

        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";  //we will save the image in the public/images
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)){  //if path does not exist we will create it
                Files.createDirectories(uploadPath);
            }

            //uploading to the path
            try (InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);

            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }


        //setting the product objects to the productDto that are being inserted from the user
        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        //now we will save the product to the database
        repo.save(product);


        return "redirect:/products";  //when creating we go back to the list

    }



    @GetMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id
            ){
        try{
            Product product = repo.findById(id).get();  //getting the id from database
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            //adding the object to the model

            model.addAttribute("productDto", productDto);


        }catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }


        return "products/EditProduct";
    }

    //updating the product details when submitting
    @PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
            ){

        try{
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()){
                return "products/EditProduct";
            }

            if (!productDto.getImageFile().isEmpty()){
                //delete old image
                String  uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                }
                catch (Exception e){
                    System.out.println("Exception: " + e.getMessage());
                }

                //save new image file

                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setDescription(productDto.getDescription());


            repo.save(product);  //saving the product that we saved on it the new values


        }
        catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
        return "redirect:/products";
    }



    @GetMapping("/delete")
    public String deleteProduct(
            @RequestParam int id){
        try {
            //accessing the database
            Product product = repo.findById(id).get();


            //delete product image from the folder path

            Path imagePath = Paths.get("public/images/" + product.getImageFileName());

            try{
                Files.delete(imagePath);
            }
            catch (Exception e){
                System.out.println("Exception: " + e.getMessage());
            }

            //delete from db using repo
            repo.delete(product);

        }
        catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }


        return "redirect:/products";
    }



}
