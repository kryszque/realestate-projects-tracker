package com.mcdevka.realestate_projects_tracker.item;

import com.mcdevka.realestate_projects_tracker.project.Project;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects/{projectId}/pillars/{pillarId}/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {this.itemService = itemService;}

    @GetMapping
    public ResponseEntity<List<Item>> getItemsForPillar(@PathVariable Long pillarId) {
        try {
            List<Item> items = itemService.getItemsForPillar(pillarId);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<Item> createItem(
            @PathVariable Long pillarId,
            @RequestBody Item item
    ) {
        try {
            Item createdItem = itemService.createItem(pillarId, item);
            return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        try {
            Item item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item updatedItemData) {
        try {
            Item updatedItem = itemService.updateItem(id, updatedItemData);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Item> archiveItem(@PathVariable Long id) {
        try{
            Item archivedItem = itemService.archiveItem(id);
            return ResponseEntity.ok(archivedItem);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<Item> finishItem(@PathVariable Long id) {
        try{
            Item finishedItem = itemService.finishItem(id);
            return ResponseEntity.ok(finishedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
