package com.example.testando_thymeleaf.controller;

import com.example.testando_thymeleaf.dtos.ProductDto;
import com.example.testando_thymeleaf.entity.Product;
import com.example.testando_thymeleaf.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Transactional
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository repository;


    @GetMapping("/list")
    public ModelAndView showProducts() {
        List<Product> products = repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<Product> formattedProducts = products.stream()
                .map(product -> {
                    if (product.getCreatedAt() != null) {
                        product.setFormattedCreatedAt(product.getCreatedAt().format(dtf));
                    } else {
                        product.setFormattedCreatedAt("N/A");
                    }
                    return product;
                })
                .toList();

        ModelAndView mv = new ModelAndView("index");
        mv.addObject("products", formattedProducts);
        return mv;
    }

    @GetMapping("/create")
    public ModelAndView showCreatePage() {
        ProductDto productDto = new ProductDto();
        LocalDateTime data = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = data.format(dtf);
        ModelAndView mv = new ModelAndView("CreateProduct");
        mv.addObject("productDto", productDto);
        mv.addObject("data", formattedDate);
        return mv;
    }

    @PostMapping("/create")
    public ModelAndView createProduct(@Valid @ModelAttribute("productDto") ProductDto productDto, BindingResult bindingResult, RedirectAttributes redirect) {
        if (productDto.getName().isEmpty()) {
            bindingResult.addError(new FieldError("productDto", "name", "The name can't be empty"));
        }

        if (bindingResult.hasErrors()) {
            return new ModelAndView("CreateProduct");
        }

        var product = new Product();
        BeanUtils.copyProperties(productDto, product);
        product.setCreatedAt(LocalDateTime.now());

        MultipartFile imageFile = productDto.getImageFile();
        if (!imageFile.isEmpty()) {
            try {
                String imagePath = "src/main/resources/static/images/";
                String imageFileName = imageFile.getOriginalFilename();
                Path path = Paths.get(imagePath + imageFileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                product.setImageFileName(imageFileName);
            } catch (IOException e) {
                e.printStackTrace();
                bindingResult.addError(new FieldError("productDto", "imageFile", "Failed to save image file"));
                return new ModelAndView("CreateProduct");
            }
        }

        repository.save(product);
        redirect.addFlashAttribute("message", "Product created successfully");
        return new ModelAndView("redirect:/products/list");
    }

    @Transactional
    @PostMapping("/delete/{id}")
    public ModelAndView delete(@PathVariable("id") Long id){
        repository.deleteById(id);
        return new ModelAndView("redirect:/products/list");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditPage(@PathVariable("id") Long id){
        Product product = repository.findById(id).orElseThrow();
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        BeanUtils.copyProperties(product, productDto);
        ModelAndView mv = new ModelAndView("EditProduct");
        mv.addObject("productDto", productDto);
        return mv;
    }

    @PostMapping("/edit/{id}")
    public ModelAndView updateProduct(@PathVariable("id") Long id, @Valid @ModelAttribute("productDto") ProductDto productDto, BindingResult bindingResult, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("EditProduct");
        }

        Product product = repository.findById(id).orElseThrow();
        BeanUtils.copyProperties(productDto, product);
        product.setCreatedAt(LocalDateTime.now());

        MultipartFile imageFile = productDto.getImageFile();
        if (!imageFile.isEmpty()) {
            try {
                String imagePath = "src/main/resources/static/images/";
                String imageFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path path = Paths.get(imagePath + imageFileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                product.setImageFileName(imageFileName);
            } catch (IOException e) {
                e.printStackTrace();
                bindingResult.addError(new FieldError("productDto", "imageFile", "Failed to save image file"));
                return new ModelAndView("EditProduct");
            }
        }

        repository.save(product);
        redirect.addFlashAttribute("message", "Product updated successfully");
        return new ModelAndView("redirect:/products/list");
    }



    @GetMapping("/search")
    public ModelAndView searchProducts(@RequestParam("searchTerm") String searchTerm) {
        List<Product> products = repository.findByNameContaining(searchTerm);
        ModelAndView mv = new ModelAndView("index");
        mv.addObject("products", products);
        return mv;
    }


}
