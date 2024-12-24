package com.example.userservice.service;

import java.util.List;
import java.util.UUID;

import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.response.UserResponse;

/**
 * User関連の操作を定義するServiceInterface。
 * マイクロサービスアーキテクチャにおいて、UUIDを使用することで
 * 各サービスでのUserIDのユニーク性を保持する。
 */
public interface UserService {
    /**
     * 新規Userを作成。
     * @param request User作成リクエスト
     * @return 作成されたUserの情報
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * 指定されたIDのUserUser情報を更新。
     * @param id 更新対象UserのUUID
     * @param request 更新内容を含むリクエスト
     * @return 更新されたUserの情報
     */
    UserResponse updateUser(UUID id, CreateUserRequest request);

    /**
     * 指定されたIDのUser情報を取得します。
     * @param id 取得対象UserのUUID
     * @return Userの情報
     */
    UserResponse getUserById(UUID id);

    /**
     * すべてのUser情報を取得します。
     * @return User情報のリスト
     */
    List<UserResponse> getAllUsers();

    /**
     * 指定されたIDのUserを削除します。
     * @param id 削除対象UserのUUID
     */
    void deleteUser(UUID id);
}