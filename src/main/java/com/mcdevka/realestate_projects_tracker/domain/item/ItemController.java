package com.mcdevka.realestate_projects_tracker.domain.item;

import com.mcdevka.realestate_projects_tracker.domain.project.Project;
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
    public ResponseEntity<List<Item>> getItemsForPillar(@PathVariable Long projectId, @PathVariable Long pillarId) {
        try {
            List<Item> items = itemService.getItemsForPillar(projectId, pillarId);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@PathVariable Long projectId, @PathVariable Long pillarId, @RequestBody Item item) {
        try {
            Item createdItem = itemService.createItem(projectId, pillarId, item);
            return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id) {
        try {
            Item item = itemService.getItemById(projectId, pillarId, id);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @RequestBody Item updatedItemData) {
        try {
            Item updatedItem = itemService.updateItem(projectId, pillarId, id, updatedItemData);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Item> archiveItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id) {
        try{
            Item archivedItem = itemService.archiveItem(projectId, pillarId, id);
            return ResponseEntity.ok(archivedItem);
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<Item> finishItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id) {
        try{
            Item finishedItem = itemService.finishItem(projectId, pillarId, id);
            return ResponseEntity.ok(finishedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Item> addTagToItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @PathVariable Long tagId) {
        try {
            Item updatedItem = itemService.addTagToItem(projectId, pillarId, id, tagId);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Item> removeTagFromItem(@PathVariable Long projectId, @PathVariable Long pillarId , @PathVariable Long id, @PathVariable Long tagId) {
        try {
            Item updatedItem = itemService.removeTagFromItem(projectId, pillarId, id, tagId);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

//    @GetMapping("/search")
//    public ResponseEntity<List<Item>> searchItems(
//            @ModelAttribute ItemSearchCriteria criteria){
//        List<Item> result =  itemService.searchItems(criteria);
//        return ResponseEntity.ok(result);
//    }
}
