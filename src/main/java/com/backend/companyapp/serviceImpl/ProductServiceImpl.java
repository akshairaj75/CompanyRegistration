package com.backend.companyapp.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.companyapp.dto.product.ProductRequestDto;
import com.backend.companyapp.dto.product.ProductResponseDto;
import com.backend.companyapp.entity.Brand;
import com.backend.companyapp.entity.Category;
import com.backend.companyapp.entity.Product;
import com.backend.companyapp.repository.BrandRepository;
import com.backend.companyapp.repository.CategoryRepository;
import com.backend.companyapp.repository.ProductRepository;
import com.backend.companyapp.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final FileStorageService fileStorageService;    

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
            BrandRepository brandRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductResponseDto::fromEntity).toList();
    }

    @Override
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponseDto.fromEntity(product);
    }

    @Override
    public ProductResponseDto createProduct(MultipartFile file, ProductRequestDto productRequestDto) {

        Category category = categoryRepository.findById(productRequestDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Brand brand = brandRepository.findById(productRequestDto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        String fileName;

        Category subCategory = null;
        if (productRequestDto.getSubCategoryId() != null) {
            subCategory = categoryRepository.findById(productRequestDto.getSubCategoryId())
                    .orElseThrow(() -> new RuntimeException("Subcategory not found"));
            if (subCategory.getParent() != null && !subCategory.getParent().getId().equals(category.getId())) {
                throw new RuntimeException("Selected subcategory does not belong to the selected category");
            }
        }

        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);
        product.setFeatured(productRequestDto.isFeatured());

        if (file != null && !file.isEmpty()) {
            try {
                fileName = fileStorageService.storeFile(file, "products");
                product.setImage(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to store product image", e);
            }
        }
        Product savedProduct = productRepository.save(product);
        return ProductResponseDto.fromEntity(savedProduct);
    }

    @Override
    public ProductResponseDto updateProduct(Long id, MultipartFile file, ProductRequestDto productRequestDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        Category category = categoryRepository.findById(productRequestDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Brand brand = brandRepository.findById(productRequestDto.getBrandId()).orElseThrow(() -> new RuntimeException("Brand not found"));
        String fileName;

        Category subCategory = null;
        if (productRequestDto.getSubCategoryId() != null) {
            subCategory = categoryRepository.findById(productRequestDto.getSubCategoryId())
                    .orElseThrow(() -> new RuntimeException("Subcategory not found"));
            if (subCategory.getParent() != null && !subCategory.getParent().getId().equals(category.getId())) {
                throw new RuntimeException("Selected subcategory does not belong to the selected category");
            }
        }

        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);
        product.setFeatured(productRequestDto.isFeatured());

        if (file != null && !file.isEmpty()) {
            try {
                String oldProductImage = product.getImage();
                fileName = fileStorageService.storeFile(file, "products");
                product.setImage(fileName);
                if (oldProductImage != null && !oldProductImage.trim().isEmpty()) {
                    fileStorageService.deleteFile(oldProductImage, "products");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to update product image", e);
            }
        }
        Product savedProduct = productRepository.save(product);
        return ProductResponseDto.fromEntity(savedProduct);  
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getImage() != null && !product.getImage().trim().isEmpty()) {
            fileStorageService.deleteFile(product.getImage(), "products");
        }
        if (product.getCompanies() != null) {
            product.getCompanies().forEach(company -> company.getProducts().remove(product));
            product.getCompanies().clear();
        }
        productRepository.delete(product);
    }
}
